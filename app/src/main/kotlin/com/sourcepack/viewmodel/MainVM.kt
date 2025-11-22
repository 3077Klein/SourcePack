package com.sourcepack.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sourcepack.core.*
import com.sourcepack.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class UiState {
    data object Idle : UiState()
    data class Loading(val msg: String, val detail: String = "") : UiState()
    data class Success(val info: String, val uri: Uri?) : UiState()
    // 修改：错误状态携带日志文件的 URI
    data class Error(val err: String, val logUri: Uri? = null) : UiState()
}

class MainVM(app: Application) : AndroidViewModel(app) {
    private val prefs = PreferenceManager(app)
    
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state = _state.asStateFlow()
    
    private val _cfg = MutableStateFlow(prefs.config)
    val cfg = _cfg.asStateFlow()
    
    private val _theme = MutableStateFlow(prefs.theme)
    val theme = _theme.asStateFlow()
    
    private val _uFiles = MutableStateFlow(prefs.getSet("u_files"))
    val uFiles = _uFiles.asStateFlow()
    private val _uExts = MutableStateFlow(prefs.getSet("u_exts"))
    val uExts = _uExts.asStateFlow()

    init {
        prefs.initDefaultsIfNeeded()
        _uFiles.value = prefs.getSet("u_files")
        _uExts.value = prefs.getSet("u_exts")
    }

    fun saveCfg(c: PackerConfig) { prefs.config = c; _cfg.value = c }
    fun setTheme(t: AppTheme) { prefs.theme = t; _theme.value = t }
    fun reset() { _state.value = UiState.Idle }

    fun addBlacklist(type: Int, items: List<String>) {
        val key = if(type == 0) "u_files" else "u_exts"
        val current = prefs.getSet(key).toMutableSet()
        items.forEach { item ->
            val cleanItem = if(type == 1 && !item.startsWith(".")) ".$item" else item
            current.add(cleanItem)
        }
        prefs.updateSet(key, current)
        refreshLists()
    }

    fun removeBlacklist(type: Int, items: List<String>) {
        val key = if(type == 0) "u_files" else "u_exts"
        val current = prefs.getSet(key).toMutableSet()
        current.removeAll(items.toSet())
        prefs.updateSet(key, current)
        refreshLists()
    }
    
    private fun refreshLists() {
        _uFiles.value = prefs.getSet("u_files")
        _uExts.value = prefs.getSet("u_exts")
    }

    private val progressCb = object : SourcePacker.ProgressCallback {
        private var lastUpdate = 0L
        override fun onProgress(currentFile: String) {
            val now = System.currentTimeMillis()
            if (now - lastUpdate > 100) {
                lastUpdate = now
                val current = _state.value
                if (current is UiState.Loading) {
                    _state.value = current.copy(detail = currentFile)
                }
            }
        }
    }

    // --- 核心逻辑：生成错误堆栈文件 ---
    private fun saveCrashLog(e: Throwable): Uri? {
        return try {
            // 1. 获取当前时间戳
            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Error_Log_$time.txt"
            
            // 2. 获取堆栈信息字符串
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val stackTrace = sw.toString()
            
            // 3. 写入文件 (存放在 ExternalFilesDir，方便 FileProvider 分享)
            val logDir = getApplication<Application>().getExternalFilesDir("crash_logs")
            if (logDir != null && !logDir.exists()) logDir.mkdirs()
            
            val file = File(logDir, fileName)
            file.writeText("""
                === SourcePack Crash Log ===
                Time: $time
                Device: ${android.os.Build.MODEL} (${android.os.Build.VERSION.SDK_INT})
                Version: ${Str.APP_VERSION}
                
                === Exception ===
                ${e.message}
                
                === Stack Trace ===
                $stackTrace
            """.trimIndent())
            
            // 4. 生成供外部访问的 URI
            FileProvider.getUriForFile(
                getApplication(),
                "${getApplication<Application>().packageName}.provider",
                file
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun packDirectly(srcUri: Uri, destUri: Uri) {
        _state.value = UiState.Loading(Str.get("正在处理...", "Processing..."))
        viewModelScope.launch {
            try {
                SourcePacker.packToStream(
                    getApplication(), srcUri, destUri, 
                    _uFiles.value, _uExts.value, _cfg.value, progressCb
                )
                _state.value = UiState.Success("Saved to: ${destUri.path}", destUri)
            } catch (e: Exception) {
                // 捕获异常并生成日志
                val logUri = saveCrashLog(e)
                _state.value = UiState.Error(e.message ?: "Unknown Error", logUri)
            }
        }
    }

    fun packListDirectly(srcUris: List<Uri>, destUri: Uri) {
        _state.value = UiState.Loading(Str.get("正在处理...", "Processing..."))
        viewModelScope.launch {
            try {
                SourcePacker.packListToStream(
                    getApplication(), srcUris, destUri, _cfg.value, progressCb
                )
                _state.value = UiState.Success("Saved to: ${destUri.path}", destUri)
            } catch (e: Exception) {
                val logUri = saveCrashLog(e)
                _state.value = UiState.Error(e.message ?: "Unknown Error", logUri)
            }
        }
    }
    
    fun runGit(url: String, destUri: Uri) {
        var cleanUrl = url.trim().removeSuffix("/")
        if (cleanUrl.endsWith(".git")) cleanUrl = cleanUrl.removeSuffix(".git")
        if (!cleanUrl.contains("github.com")) {
            _state.value = UiState.Error("Invalid GitHub URL")
            return
        }

        val path = cleanUrl.substringAfter("github.com/")
        val finalPath = if (path.contains("/tree/")) path.substringBefore("/tree/") else path
        val zipUrl = "https://github.com/$finalPath/archive/HEAD.zip"

        _state.value = UiState.Loading(Str.get("正在下载仓库...", "Downloading Repo..."))

        viewModelScope.launch {
            try {
                SourcePacker.packGitHubRepo(
                    zipUrl, destUri, getApplication(),
                    _uFiles.value, _uExts.value, _cfg.value, progressCb
                )
                _state.value = UiState.Success("GitHub Repo Exported", destUri)
            } catch (e: Exception) {
                e.printStackTrace()
                val logUri = saveCrashLog(e)
                _state.value = UiState.Error("Error: ${e.message}", logUri)
            }
        }
    }
}