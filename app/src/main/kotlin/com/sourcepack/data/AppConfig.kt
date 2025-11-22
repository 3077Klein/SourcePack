package com.sourcepack.data

/**
 * 打包器配置模型
 * 包含所有影响打包结果的开关和选项
 */
data class PackerConfig(
    val compress: Boolean = false,      // 是否压缩内容（去除多余空白行）
    val ignoreGit: Boolean = true,      // 是否忽略 .git 目录
    val ignoreBuild: Boolean = true,    // 是否忽略 build 构建产物目录
    val ignoreGradle: Boolean = true,   // 是否忽略 .gradle 缓存目录
    val useGitIgnore: Boolean = true,   // 是否解析并应用 .gitignore 规则（预留字段）
    val format: Format = Format.MARKDOWN, // 输出文件格式
    val mode: Mode = Mode.FULL          // 输出模式：完整内容或仅目录树
)

enum class Format { MARKDOWN, XML, TEXT }
enum class Mode { FULL, TREE }
enum class AppTheme { SYSTEM, BLUE, PURPLE, GRAY }