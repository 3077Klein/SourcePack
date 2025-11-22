package com.sourcepack.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sourcepack.core.*
import com.sourcepack.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI 状态封装
 */
sealed class UiState {
    data object Idle : UiState() // 空闲状态
    data class Loading(val msg: String, val detail: String = "") : UiState() // 加载中
    data class Success(val info: String, val uri: Uri?) : UiState() // 操作成功
    data class Error(val err: String) : UiState() // 发生错误
}

/**
 * 主 ViewModel
 * 负责管理应用状态、处理业务逻辑以及与 Packer 核心交互
 */
class MainVM(app: Application) : AndroidViewModel(app) {
    private val prefs = PreferenceManager(app)
    
    // UI 状态流
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state = _state.asStateFlow()
    
    // 配置信息流
    private val _cfg = MutableStateFlow(prefs.config)
    val cfg = _cfg.asStateFlow()
    
    // 主题设置流
    private val _theme = MutableStateFlow(prefs.theme)
    val theme = _theme.asStateFlow()
    
    // 黑名单规则流
    private val _uFiles = MutableStateFlow(prefs.getSet("u_files"))
    val uFiles = _uFiles.asStateFlow()
    private val _uExts = MutableStateFlow(prefs.getSet("u_exts"))
    val uExts = _uExts.asStateFlow()

    init {
        // 确保默认黑名单被加载
        prefs.initDefaultsIfNeeded()
        _uFiles.value = prefs.getSet("u_files")
        _uExts.value = prefs.getSet("u_exts")
    }

    // 更新配置
    fun saveCfg(c: PackerConfig) { prefs.config = c; _cfg.value = c }
    
    // 切换主题
    fun setTheme(t: AppTheme) { prefs.theme = t; _theme.value = t }
    
    // 重置 UI 状态
    fun reset() { _state.value = UiState.Idle }

    // 添加黑名单规则 (0=文件名, 1=后缀)
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

    // 移除黑名单规则
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

    // 进度回调处理，防止 UI 更新过频
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

    // 执行本地文件夹打包
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
                _state.value = UiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    // 执行多文件打包
    fun packListDirectly(srcUris: List<Uri>, destUri: Uri) {
        _state.value = UiState.Loading(Str.get("正在处理...", "Processing..."))
        viewModelScope.launch {
            try {
                SourcePacker.packListToStream(
                    getApplication(), srcUris, destUri, _cfg.value, progressCb
                )
                _state.value = UiState.Success("Saved to: ${destUri.path}", destUri)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
    
    // 执行 GitHub 仓库下载并打包
    fun runGit(url: String, destUri: Uri) {
        var cleanUrl = url.trim().removeSuffix("/")
        if (cleanUrl.endsWith(".git")) cleanUrl = cleanUrl.removeSuffix(".git")
        if (!cleanUrl.contains("github.com")) {
            _state.value = UiState.Error("Invalid GitHub URL")
            return
        }

        // 构建 HEAD 分支下载链接
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
                _state.value = UiState.Error("Error: ${e.message}")
            }
        }
    }
}