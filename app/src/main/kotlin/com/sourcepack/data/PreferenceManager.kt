package com.sourcepack.data

import android.content.Context

/**
 * 偏好设置管理器
 * 封装 SharedPreferences，负责存取用户配置、黑名单规则和主题设置。
 */
class PreferenceManager(context: Context) {
    private val p = context.getSharedPreferences("sp_cfg", Context.MODE_PRIVATE)

    // 仅内部使用：检查是否已初始化过默认值
    private var isInitialized: Boolean
        get() = p.getBoolean("init_done", false)
        set(v) = p.edit().putBoolean("init_done", v).apply()

    /**
     * 初始化默认的黑名单文件和后缀
     * 仅在应用首次安装运行时执行一次
     */
    fun initDefaultsIfNeeded() {
        // 如果之前使用的是 "first" 键（旧版本兼容），或者还没初始化过
        val legacyFirst = p.getBoolean("first", true)
        
        if (!isInitialized && legacyFirst) {
            val defaultFiles = setOf("local.properties", ".DS_Store", "thumbs.db", "desktop.ini")
            val defaultExts = setOf(
                ".png", ".jpg", ".jpeg", ".webp", ".gif", ".ico", ".svg", ".bmp",
                ".mp4", ".mp3", ".wav", ".ogg",
                ".jar", ".dex", ".so", ".apk", ".aab",
                ".zip", ".7z", ".rar", ".tar", ".gz",
                ".db", ".sqlite", ".class", ".exe", ".dll", ".bin",
                ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx"
            )
            
            p.edit()
                .putStringSet("u_files", defaultFiles)
                .putStringSet("u_exts", defaultExts)
                // 标记为已初始化
                .putBoolean("init_done", true)
                // 清除旧标志（可选）
                .remove("first")
                .apply()
        }
    }

    var theme: AppTheme
        get() = AppTheme.values().getOrElse(p.getInt("app_theme", 0)) { AppTheme.SYSTEM }
        set(v) = p.edit().putInt("app_theme", v.ordinal).apply()

    // 将 PackerConfig 对象映射到 SP 存储
    var config: PackerConfig
        get() = PackerConfig(
            p.getBoolean("c_zip", false),
            p.getBoolean("c_git", true),
            p.getBoolean("c_bld", true),
            p.getBoolean("c_grd", true),
            p.getBoolean("c_gign", true),
            Format.values().getOrElse(p.getInt("c_fmt", 0)) { Format.MARKDOWN },
            Mode.values().getOrElse(p.getInt("c_mod", 0)) { Mode.FULL }
        )
        set(v) {
            p.edit()
                .putBoolean("c_zip", v.compress)
                .putBoolean("c_git", v.ignoreGit)
                .putBoolean("c_bld", v.ignoreBuild)
                .putBoolean("c_grd", v.ignoreGradle)
                .putBoolean("c_gign", v.useGitIgnore)
                .putInt("c_fmt", v.format.ordinal)
                .putInt("c_mod", v.mode.ordinal)
                .apply()
        }

    fun getSet(k: String): Set<String> = p.getStringSet(k, emptySet()) ?: emptySet()
    fun updateSet(k: String, newSet: Set<String>) = p.edit().putStringSet(k, newSet).apply()
}