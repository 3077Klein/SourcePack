package org.source.pack.core.utils

import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * SourcePacker (Desktop/JVM Version)
 * 适用于 Windows/Linux/macOS 的源码打包工具类
 */
class SourcePacker {

    // --- 配置模型 ---
    data class Config(
        val compress: Boolean = false,         // 是否压缩 (去除多余空行)
        val ignoreGit: Boolean = true,         // 忽略 .git
        val ignoreBuild: Boolean = true,       // 忽略 build
        val ignoreGradle: Boolean = true,      // 忽略 .gradle
        val format: Format = Format.MARKDOWN,  // 输出格式
        val mode: Mode = Mode.FULL,            // 输出模式
        val userIgnoreFiles: Set<String> = emptySet(), // 用户黑名单 (文件名)
        val userIgnoreExts: Set<String> = emptySet()   // 用户黑名单 (后缀)
    )

    enum class Format { MARKDOWN, XML, TEXT }
    enum class Mode { FULL, TREE }

    // --- 进度回调 ---
    fun interface ProgressCallback {
        fun onProgress(currentFile: String)
    }

    companion object {
        // 强制忽略的目录 (移除可配置项，只保留系统级垃圾文件)
        private val FORCE_IGNORE_DIRS = setOf(
            ".svn", ".idea", ".vscode", "node_modules",
            "captures", "__pycache__", ".DS_Store"
        )

        // 二进制文件后缀
        private val BINARY_EXTS = setOf(
            ".zip", ".7z", ".rar", ".tar", ".gz", ".apk", ".jar",
            ".png", ".jpg", ".jpeg", ".webp", ".gif", ".ico", ".svg",
            ".so", ".dll", ".exe", ".class", ".dex",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".mp3", ".mp4", ".wav", ".ogg",
            ".db", ".sqlite",
            ".ttf", ".woff", ".eot",
            ".psd", ".ai", ".obj", ".lib"
        )

        private const val MAX_FILE_SIZE = 1024 * 1024L // 1MB 限制
        private const val BUFFER_SIZE = 16 * 1024
    }

    // =========================== 公开方法 ===========================

    /**
     * 打包本地文件夹
     */
    suspend fun packLocal(
        sourceDir: File,
        destFile: File,
        config: Config,
        callback: ProgressCallback? = null
    ) = withContext(Dispatchers.IO) {
        if (!sourceDir.exists()) throw FileNotFoundException("Source dir not found: ${sourceDir.absolutePath}")

        // 确保目标父目录存在
        destFile.parentFile?.mkdirs()

        val rootNode = LocalFastFile(sourceDir)
        processPacking(rootNode, destFile, config, callback)
    }

    /**
     * 下载并打包 GitHub 仓库
     */
    suspend fun packGitHub(
        repoUrl: String,
        destFile: File,
        config: Config,
        callback: ProgressCallback? = null
    ) = withContext(Dispatchers.IO) {
        // 解析 URL
        var cleanUrl = repoUrl.trim().removeSuffix("/")
        if (cleanUrl.endsWith(".git")) cleanUrl = cleanUrl.removeSuffix(".git")

        val path = cleanUrl.substringAfter("github.com/")
        val finalPath = if (path.contains("/tree/")) path.substringBefore("/tree/") else path
        val zipUrl = "https://github.com/$finalPath/archive/HEAD.zip"
        val projectName = finalPath.substringAfterLast("/")

        // 创建临时文件
        val tempFile = File.createTempFile("sp_gh_", ".zip")

        try {
            callback?.onProgress("Downloading $projectName...")
            downloadFile(zipUrl, tempFile)

            callback?.onProgress("Analyzing ZIP structure...")
            val zipFile = ZipFile(tempFile)
            val rootNode = buildZipVFS(zipFile, projectName)

            // 确保目标父目录存在
            destFile.parentFile?.mkdirs()

            processPacking(rootNode, destFile, config, callback)
            zipFile.close()
        } catch (e: Exception) {
            throw IOException("Failed to process GitHub repo: ${e.message}", e)
        } finally {
            tempFile.delete()
        }
    }

    // =========================== 核心处理逻辑 ===========================

    private suspend fun processPacking(
        root: FastFile,
        destFile: File,
        cfg: Config,
        cb: ProgressCallback?
    ) {
        // 使用 BufferedWriter 写入，强制 UTF-8
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(destFile), StandardCharsets.UTF_8), BUFFER_SIZE)

        try {
            val projectName = root.name
            val destFileName = destFile.name

            // 预处理过滤规则 (合并强制忽略和用户配置)
            val skipDirs = FORCE_IGNORE_DIRS.toMutableSet().apply {
                if (cfg.ignoreGradle) add(".gradle")
                if (cfg.ignoreBuild) {
                    add("build")
                    add("target") // Maven target
                }
                if (cfg.ignoreGit) add(".git")
            }

            // 确保后缀带点
            val userBinExts = cfg.userIgnoreExts.map { if (it.startsWith(".")) it else ".$it" }.toSet()

            // 写入头部
            writeHeader(writer, projectName, cfg)

            // 1. 生成目录树
            if (cfg.format != Format.XML) {
                cb?.onProgress("Generating Directory Tree...")
                writer.write("## Project Structure\n\n")
                writer.write("```text\n")
                val treeBuilder = StringBuilder()
                generateTreeString(root, "", treeBuilder, skipDirs, destFileName)
                writer.write(treeBuilder.toString())
                writer.write("```\n\n")
            }

            // 2. 遍历并写入内容
            if (cfg.mode == Mode.FULL || cfg.format == Format.XML) {
                if (cfg.format != Format.XML) {
                    writer.write("## File Contents\n\n")
                }
                processNode(root, "", writer, skipDirs, cfg.userIgnoreFiles, userBinExts, cfg, cb, destFileName)
            }

            writeFooter(writer, cfg)
            cb?.onProgress("Done!")

        } finally {
            writer.flush()
            writer.close()
        }
    }

    // 递归生成树形结构字符串
    private fun generateTreeString(
        node: FastFile,
        prefix: String,
        sb: StringBuilder,
        skipDirs: Set<String>,
        ignoreFileName: String
    ) {
        if (prefix.isEmpty()) sb.append("📦 ${node.name}\n")

        if (node.isDirectory) {
            val children = node.listFiles().sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            for (child in children) {
                // 排除输出文件本身
                if (!child.isDirectory && child.name == ignoreFileName) continue

                // 排除忽略的文件夹
                if (child.isDirectory && child.name in skipDirs) continue

                val isDir = child.isDirectory
                val icon = if (isDir) " 📂 " else " 📄 "
                sb.append(prefix).append(icon).append(child.name).append("\n")

                if (isDir) {
                    generateTreeString(child, "$prefix  ", sb, skipDirs, ignoreFileName)
                }
            }
        }
    }

    // 递归处理文件内容
    private suspend fun processNode(
        node: FastFile,
        relativePath: String,
        writer: BufferedWriter,
        skipDirs: Set<String>,
        userFiles: Set<String>,
        userExts: Set<String>,
        cfg: Config,
        cb: ProgressCallback?,
        ignoreFileName: String
    ) {
        currentCoroutineContext().ensureActive()

        if (node.isDirectory) {
            if (cfg.format == Format.XML && relativePath.isNotEmpty()) {
                writer.write("  <dir name=\"${node.name}\">\n")
            }

            val children = node.listFiles().sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            for (child in children) {
                val name = child.name
                // 排除输出文件本身
                if (!child.isDirectory && name == ignoreFileName) continue

                val childPath = if (relativePath.isEmpty()) name else "$relativePath/$name"

                if (child.isDirectory) {
                    if (name in skipDirs) continue
                } else {
                    if (name in userFiles) continue
                    if (userExts.any { name.endsWith(it, ignoreCase = true) }) continue
                }
                processNode(child, childPath, writer, skipDirs, userFiles, userExts, cfg, cb, ignoreFileName)
            }

            if (cfg.format == Format.XML && relativePath.isNotEmpty()) {
                writer.write("  </dir>\n")
            }
        } else {
            // 仅树模式跳过内容
            if (cfg.mode == Mode.TREE && cfg.format != Format.XML) return

            cb?.onProgress(relativePath)

            // 检查全局二进制后缀
            val isBinExt = BINARY_EXTS.any { node.name.endsWith(it, ignoreCase = true) }
            if (isBinExt || node.length > MAX_FILE_SIZE) return

            appendContent(node, relativePath, writer, cfg)
        }
    }

    // 读取并追加单个文件内容
    private fun appendContent(node: FastFile, path: String, writer: BufferedWriter, cfg: Config) {
        try {
            writer.write(formatHeader(path, cfg.format))

            node.openStream().use { ins ->
                val headBuffer = ByteArray(1024)
                val headReadLen = readAtMost(ins, headBuffer)

                // 检查前 1KB 是否包含 NULL 字节来判断是否为二进制
                val isBinary = if (headReadLen > 0) isBufferBinary(headBuffer, headReadLen) else false

                if (isBinary) {
                    writer.write("[Binary content detected]")
                } else {
                    // 重组流：头部 + 剩余
                    val headStream = ByteArrayInputStream(headBuffer, 0, headReadLen)
                    val combinedStream = SequenceInputStream(headStream, ins)
                    val reader = BufferedReader(InputStreamReader(combinedStream, StandardCharsets.UTF_8), 8192)

                    var line = reader.readLine()
                    while (line != null) {
                        if (cfg.compress) {
                            val trimmed = line.trim()
                            if (trimmed.isNotEmpty()) {
                                if (cfg.format == Format.XML) writer.write(escapeXml(trimmed)) else writer.write(trimmed)
                                writer.write(" ")
                            }
                        } else {
                            if (cfg.format == Format.XML) writer.write(escapeXml(line)) else writer.write(line)
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

    // --- 网络下载工具 ---
    private fun downloadFile(urlStr: String, destFile: File) {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 60000
        conn.instanceFollowRedirects = true

        conn.inputStream.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    // --- 辅助方法 ---
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

    private fun writeHeader(writer: BufferedWriter, name: String, cfg: Config) {
        if (cfg.format == Format.XML) writer.write("<project name=\"$name\">\n<files>\n")
        else writer.write("# Project: $name\n\n")
    }

    private fun writeFooter(writer: BufferedWriter, cfg: Config) {
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

    // =========================== FastFile 抽象层 ===========================

    interface FastFile {
        val name: String
        val isDirectory: Boolean
        val length: Long
        fun listFiles(): List<FastFile>
        fun openStream(): InputStream
    }

    // 本地文件实现
    private class LocalFastFile(val file: File) : FastFile {
        override val name: String get() = file.name
        override val isDirectory: Boolean get() = file.isDirectory
        override val length: Long get() = file.length()
        override fun listFiles(): List<FastFile> = file.listFiles()?.map { LocalFastFile(it) } ?: emptyList()
        override fun openStream(): InputStream = FileInputStream(file)
    }

    // ZIP 文件实现 (用于处理 GitHub 下载的 zip)
    private class ZipFastFile(
        override val name: String,
        override val isDirectory: Boolean,
        private val zipFile: ZipFile,
        private val entry: ZipEntry?,
        private val children: List<ZipFastFile> = emptyList()
    ) : FastFile {
        override val length: Long get() = entry?.size ?: 0L
        override fun listFiles(): List<FastFile> = children
        override fun openStream(): InputStream = if (entry != null) zipFile.getInputStream(entry) else ByteArrayInputStream(ByteArray(0))
    }

    // 将 ZipFile 解析为虚拟文件树
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

        val rootChildren = treeMap[""] ?: emptyList()
        // 处理 GitHub ZIP 常见的顶层文件夹包裹
        if (rootChildren.size == 1 && rootChildren[0].isDirectory) {
            val realRoot = rootChildren[0]
            return buildNode(realRoot.name.removeSuffix("/"), realRoot.name.removeSuffix("/"), realRoot)
        }

        return buildNode(projectName, "", null)
    }
}