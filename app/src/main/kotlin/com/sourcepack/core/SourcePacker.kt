package com.sourcepack.core

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.sourcepack.data.*
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object SourcePacker {
    // 系统级忽略目录：这些目录通常包含构建产物或元数据，不参与分析
    private val FORCE_IGNORE_DIRS = setOf(".git", ".svn", ".idea", ".vscode", ".gradle", "build", "target", "node_modules", "captures")
    
    // 二进制文件后缀：这些文件的内容会被跳过，但在文件树中会保留显示
    private val BINARY_EXTS = setOf(
        ".zip", ".7z", ".rar", ".tar", ".gz", ".apk", ".jar", ".png", ".jpg", ".jpeg", ".webp", ".gif", ".ico", ".svg",
        ".so", ".dll", ".exe", ".class", ".dex", ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
        ".mp3", ".mp4", ".wav", ".ogg", ".db", ".sqlite", ".ttf", ".woff", ".eot", ".psd", ".ai"
    )
    
    private const val MAX_FILE_SIZE = 1024 * 1024L // 文本内容读取上限 1MB
    private const val BUFFER_SIZE = 16 * 1024 // 16KB 写入缓冲

    interface ProgressCallback {
        fun onProgress(currentFile: String)
    }

    /**
     * 统一打包入口
     * 负责调度文件遍历、过滤、格式化写入
     */
    suspend fun packToStream(
        ctx: Context,
        root: FastFile, 
        destUri: Uri,
        userFiles: Set<String>,
        userExts: Set<String>,
        cfg: PackerConfig,
        cb: ProgressCallback
    ) = withContext(Dispatchers.IO) {
        val outputStream = ctx.contentResolver.openOutputStream(destUri, "w") ?: throw IOException("Cannot open dest URI")
        val writer = BufferedWriter(OutputStreamWriter(outputStream), BUFFER_SIZE)

        try {
            val projectName = root.name
            
            // 准备目录过滤规则 (仅用于递归时跳过特定文件夹)
            val skipDirs = FORCE_IGNORE_DIRS.toMutableSet().apply {
                if (cfg.ignoreGradle) add(".gradle")
                if (cfg.ignoreBuild) add("build")
                if (cfg.ignoreGit) add(".git")
            }
            // 准备内容过滤规则 (后缀名标准化)
            val binExts = userExts.map { if (it.startsWith(".")) it else ".$it" }.toSet()

            // 1. 写入项目头部信息
            writeHeader(writer, projectName, cfg)

            // 2. 生成并写入文件树 (Metadata)
            // 策略：除了系统级忽略目录外，展示所有文件（包括被黑名单过滤的文件），以便 AI 理解完整架构
            if (cfg.format != Format.XML) {
                cb.onProgress("Generating Tree...")
                writer.write("## Project Structure\n\n")
                writer.write("```text\n")
                val treeBuilder = StringBuilder()
                generateTreeString(root, "", treeBuilder, skipDirs)
                writer.write(treeBuilder.toString())
                writer.write("```\n\n")
            }

            // 3. 递归处理文件内容 (应用黑名单和二进制过滤)
            if (cfg.mode == Mode.FULL || cfg.format == Format.XML) {
                if (cfg.format != Format.XML) {
                    writer.write("## File Contents\n\n")
                }
                processNode(ctx, root, "", writer, skipDirs, userFiles, binExts, cfg, cb)
            }

            writeFooter(writer, cfg)
        } finally {
            try {
                writer.flush()
                writer.close()
                outputStream.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * GitHub 仓库处理逻辑
     * 1. 下载 Zip
     * 2. 在内存中构建虚拟文件系统 (VFS)
     * 3. 调用统一打包接口
     */
    suspend fun packGitHubRepo(
        urlStr: String,
        destUri: Uri,
        ctx: Context,
        userFiles: Set<String>,
        userExts: Set<String>,
        cfg: PackerConfig,
        cb: ProgressCallback
    ) = withContext(Dispatchers.IO) {
        val tempFile = File(ctx.cacheDir, "gh_temp_${System.currentTimeMillis()}.zip")
        
        try {
            cb.onProgress("Downloading...")
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 60000
            conn.instanceFollowRedirects = true
            
            conn.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            cb.onProgress("Analyzing Structure...")
            val zipFile = ZipFile(tempFile)
            // 构建内存映射树，避免解压大量小文件
            val rootNode = buildZipVFS(zipFile, urlStr.substringAfterLast("/").substringBefore("."))
            
            packToStream(ctx, rootNode, destUri, userFiles, userExts, cfg, cb)
            
            zipFile.close()
        } finally {
            tempFile.delete()
        }
    }

    /**
     * 生成可视化目录树字符串
     * 注意：此方法不过滤用户黑名单文件，只过滤 skipDirs，保证架构可见性
     */
    private fun generateTreeString(
        node: FastFile,
        prefix: String,
        sb: StringBuilder,
        skipDirs: Set<String>
    ) {
        if (prefix.isEmpty()) {
            sb.append("📦 ${node.name}\n")
        }

        if (node.isDirectory) {
            val children = node.listFiles()
                .sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            
            for (child in children) {
                val name = child.name
                
                // 仅跳过系统级忽略目录
                if (child.isDirectory && name in skipDirs) continue
                
                val isDir = child.isDirectory
                val icon = if (isDir) " 📂 " else " 📄 "
                
                sb.append(prefix).append(icon).append(name).append("\n")
                
                if (isDir) {
                    generateTreeString(child, "$prefix  ", sb, skipDirs)
                }
            }
        }
    }

    /**
     * 递归写入文件内容
     * 此处应用严格的过滤逻辑 (User Files, Binary Extensions, File Size)
     */
    private suspend fun processNode(
        ctx: Context,
        node: FastFile,
        relativePath: String,
        writer: BufferedWriter,
        skipDirs: Set<String>,
        userFiles: Set<String>,
        binExts: Set<String>,
        cfg: PackerConfig,
        cb: ProgressCallback
    ) {
        currentCoroutineContext().ensureActive()

        if (node.isDirectory) {
            // XML 格式需要保留目录层级标签
            if (cfg.format == Format.XML && relativePath.isNotEmpty()) {
                writer.write("  <dir name=\"${node.name}\">\n")
            }

            val children = node.listFiles()
                .sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            
            for (child in children) {
                currentCoroutineContext().ensureActive()
                val name = child.name
                val childPath = if (relativePath.isEmpty()) name else "$relativePath/$name"

                // 1. 目录过滤
                if (child.isDirectory) {
                    if (name in skipDirs) continue
                } else {
                    // 2. 文件内容过滤 (黑名单文件跳过写入，但在 Tree 中已展示)
                    if (name in userFiles) continue
                    if (binExts.any { name.endsWith(it, ignoreCase = true) }) continue
                }
                
                processNode(ctx, child, childPath, writer, skipDirs, userFiles, binExts, cfg, cb)
            }

            if (cfg.format == Format.XML && relativePath.isNotEmpty()) {
                writer.write("  </dir>\n")
            }

        } else {
            // 非 XML 模式下，Tree Mode 不需要写入文件内容
            if (cfg.mode == Mode.TREE && cfg.format != Format.XML) return
            
            cb.onProgress(relativePath)
            
            // 检查二进制文件和大小限制
            val isBinExt = BINARY_EXTS.any { node.name.endsWith(it, ignoreCase = true) }
            if (isBinExt || node.length > MAX_FILE_SIZE) return

            appendContent(ctx, node, relativePath, writer, cfg)
        }
    }

    // --- 文件系统抽象层 (适配 File, DocumentFile, ZipEntry) ---
    
    interface FastFile {
        val name: String
        val isDirectory: Boolean
        val length: Long
        fun listFiles(): List<FastFile>
        fun openStream(ctx: Context): InputStream
    }
    
    class JavaIoFile(val file: File) : FastFile {
        override val name: String get() = file.name
        override val isDirectory: Boolean get() = file.isDirectory
        override val length: Long get() = file.length()
        override fun listFiles(): List<FastFile> = file.listFiles()?.map { JavaIoFile(it) } ?: emptyList()
        override fun openStream(ctx: Context): InputStream = FileInputStream(file)
    }

    class DocumentFileNode(val file: DocumentFile) : FastFile {
        override val name: String get() = file.name ?: ""
        override val isDirectory: Boolean get() = file.isDirectory
        override val length: Long get() = file.length()
        override fun listFiles(): List<FastFile> = file.listFiles().map { DocumentFileNode(it) }
        override fun openStream(ctx: Context): InputStream = ctx.contentResolver.openInputStream(file.uri) ?: throw IOException()
    }

    class ZipFastFile(
        override val name: String,
        override val isDirectory: Boolean,
        private val zipFile: ZipFile,
        private val entry: ZipEntry?,
        private val children: List<ZipFastFile> = emptyList()
    ) : FastFile {
        override val length: Long get() = entry?.size ?: 0L
        override fun listFiles(): List<FastFile> = children
        override fun openStream(ctx: Context): InputStream = if (entry != null) zipFile.getInputStream(entry) else ByteArrayInputStream(ByteArray(0))
    }

    // --- Zip VFS 构建逻辑 ---
    private fun buildZipVFS(zipFile: ZipFile, projectName: String): ZipFastFile {
        val treeMap = mutableMapOf<String, MutableList<ZipEntry>>()
        val entries = zipFile.entries()
        
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val path = entry.name.removeSuffix("/")
            if (path.isEmpty()) continue
            val parentPath = if (path.contains("/")) path.substringBeforeLast("/") else ""
            treeMap.getOrPut(parentPath) { mutableListOf() }.add(entry)
        }
        
        fun buildNode(name: String, path: String, entry: ZipEntry?): ZipFastFile {
            val isDir = entry?.isDirectory ?: true
            val childrenEntries = treeMap[path] ?: emptyList()
            val childrenNodes = childrenEntries.map { childEntry ->
                val childName = childEntry.name.removeSuffix("/").substringAfterLast("/")
                val childPath = childEntry.name.removeSuffix("/")
                buildNode(childName, childPath, childEntry)
            }
            return ZipFastFile(name, isDir, zipFile, entry, childrenNodes)
        }
        
        // 处理 GitHub Zip 包通常包含一层根目录的情况
        val rootChildren = treeMap[""] ?: emptyList()
        if (rootChildren.size == 1 && rootChildren[0].isDirectory) {
            val realRoot = rootChildren[0]
            return buildNode(realRoot.name.removeSuffix("/"), realRoot.name.removeSuffix("/"), realRoot)
        }
        return buildNode(projectName, "", null)
    }

    // --- 写入辅助方法 ---

    suspend fun packToStream(ctx: Context, rootUri: Uri, destUri: Uri, uFiles: Set<String>, uExts: Set<String>, cfg: PackerConfig, cb: ProgressCallback) {
        val rootNode: FastFile = if (rootUri.scheme == "file") {
            JavaIoFile(File(rootUri.path!!))
        } else {
            DocumentFileNode(DocumentFile.fromTreeUri(ctx, rootUri)!!)
        }
        packToStream(ctx, rootNode, destUri, uFiles, uExts, cfg, cb)
    }

    private fun appendContent(ctx: Context, node: FastFile, path: String, writer: BufferedWriter, cfg: PackerConfig) {
        try {
            writer.write(formatHeader(path, cfg.format))
            node.openStream(ctx).use { ins ->
                // 预读检测二进制
                val headBuffer = ByteArray(1024)
                val headReadLen = readAtMost(ins, headBuffer)
                val isBinary = if (headReadLen > 0) isBufferBinary(headBuffer, headReadLen) else false
                
                if (isBinary) {
                    writer.write("[Binary content detected]")
                } else {
                    val headStream = ByteArrayInputStream(headBuffer, 0, headReadLen)
                    val combinedStream = SequenceInputStream(headStream, ins)
                    val reader = BufferedReader(InputStreamReader(combinedStream), 8192)
                    
                    // 逐行读取并处理压缩选项
                    var line = reader.readLine()
                    while (line != null) {
                         if (cfg.compress) {
                            val trimmed = line.trim()
                            if (trimmed.isNotEmpty()) {
                                if (cfg.format == Format.XML) writer.write(escapeXml(trimmed))
                                else writer.write(trimmed)
                                writer.write(" ")
                            }
                        } else {
                             if (cfg.format == Format.XML) writer.write(escapeXml(line))
                             else writer.write(line)
                             writer.write("\n")
                        }
                        line = reader.readLine()
                    }
                }
            }
            writer.write(formatFooter(cfg.format))
        } catch (e: Exception) {
            writer.write("\n[Read Error: ${e.message}]\n")
        }
    }

    private fun readAtMost(input: InputStream, buffer: ByteArray): Int {
        var total = 0
        while (total < buffer.size) {
            val count = input.read(buffer, total, buffer.size - total)
            if (count == -1) break
            total += count
        }
        return total
    }

    private fun isBufferBinary(buf: ByteArray, len: Int): Boolean {
        for (i in 0 until len) if (buf[i] == 0.toByte()) return true
        return false
    }

    private fun writeHeader(writer: BufferedWriter, name: String, cfg: PackerConfig) {
        if (cfg.format == Format.XML) writer.write("<project name=\"$name\">\n<files>\n")
        else writer.write("# Project: $name\n\n")
    }

    private fun writeFooter(writer: BufferedWriter, cfg: PackerConfig) {
        if (cfg.format == Format.XML) writer.write("</files>\n</project>")
    }

    private fun formatHeader(name: String, format: Format): String {
        return when (format) {
            Format.MARKDOWN -> "\n## $name\n```${name.substringAfterLast('.', "")}\n"
            Format.XML -> "\n<file path=\"$name\">\n"
            Format.TEXT -> "\n--- $name ---\n"
        }
    }

    private fun formatFooter(format: Format): String {
        return when (format) {
            Format.MARKDOWN -> "```\n"
            Format.XML -> "</file>\n"
            Format.TEXT -> "\n"
        }
    }
    
    private fun escapeXml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    // 处理多文件选择模式
    suspend fun packListToStream(ctx: Context, uris: List<Uri>, destUri: Uri, cfg: PackerConfig, cb: ProgressCallback) = withContext(Dispatchers.IO) {
        val outputStream = ctx.contentResolver.openOutputStream(destUri, "w") ?: return@withContext
        val writer = BufferedWriter(OutputStreamWriter(outputStream), BUFFER_SIZE)
        writer.write(if (cfg.format == Format.XML) "<file_list>\n" else "# Selected Files\n\n")
        
        uris.forEach { uri ->
            val df = DocumentFile.fromSingleUri(ctx, uri) ?: return@forEach
            cb.onProgress(df.name ?: "unknown")
            appendContent(ctx, DocumentFileNode(df), df.name ?: "unknown", writer, cfg)
        }
        
        if (cfg.format == Format.XML) writer.write("</file_list>")
        writer.flush(); writer.close()
    }
}