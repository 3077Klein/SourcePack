# 📦 SourcePack

**SourcePack** 是一个基于 Kotlin Compose Desktop 开发的源码打包工具。它可以将整个项目（本地文件夹或 GitHub 仓库）的源代码合并成一个单独的文本文件（Markdown、XML 或纯文本）。

**主要用途：** 方便将大型项目的代码上下文一次性投喂给 ChatGPT、Claude、DeepSeek 等 AI 模型，用于代码分析、重构或学习。

![alt text](https://img.shields.io/badge/License-Apache%202.0-blue.svg) ![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple.svg) ![Compose](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)

## ✨ 主要功能

*   **📂 多源支持**：支持选择 **本地文件夹** 或直接输入 **GitHub 仓库链接**（自动下载并提取）。
*   **🛠️ 智能过滤**：
    *   自动识别并忽略二进制文件（图片、视频、可执行文件等）。
    *   内置常见忽略规则（`.git`, `node_modules`, `build`, `.gradle`, `.idea` 等）。
    *   支持自定义忽略文件名和后缀名。
*   **🎨 输出格式化**：
    *   **Markdown**：生成带有语法高亮的代码块，适合阅读和 LLM 解析。
    *   **XML**：结构化输出，适合特定 Prompt 工程。
    *   **纯文本**：最原始的拼接。
    *   **仅目录树**：仅生成项目结构图。
*   **⚡ 优化与压缩**：支持移除代码中的空行，节省 Token 消耗。
*   **🖼️ Fluent Design**：采用 Windows 11 原生风格 UI（Mica材质、亚克力效果），支持深色/浅色模式跟随系统。

## 📸 截图

<img width="1000" height="1229" alt="d3da07eb4267a2e6d185be071f05d7d3" src="https://github.com/user-attachments/assets/76694f96-d0b8-49ed-81e1-afe8c46ad992" />


## 🛠️ 技术栈

本项目使用 Kotlin 构建：

*   **语言**: [Kotlin](https://kotlinlang.org/)
*   **UI 框架**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Desktop/JVM)
*   **设计系统**: [Compose Fluent](https://github.com/Compose-Fluent/Fluent) (Windows 11 Design System)
*   **系统集成**: [JNA](https://github.com/java-native-access/jna) (用于 Windows 窗口效果、无边框窗口处理)
*   **构建工具**: Gradle Kotlin DSL

## 📖 使用指南

1.  **输入源**：
    *   选择“本地文件夹”并点击“浏览”选择项目根目录。
    *   或者选择“GitHub 仓库”并粘贴仓库 URL (如 `https://github.com/user/repo`)。
2.  **输出目标**：
    *   设置生成的 `.md` 或 `.xml` 文件保存位置。
3.  **高级配置** (可选)：
    *   展开高级配置，勾选“压缩内容”以节省 Token。
    *   在忽略列表中添加您不想包含的敏感文件（如 `secret.key`）。
    *   切换输出格式（推荐使用 Markdown）。
4.  **开始打包**：
    *   点击右下角的“开始打包”按钮，等待处理完成。

## 🤝 贡献

欢迎提交 Issue 或 Pull Request！

## 📄 许可证

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)许可证开源 - 详见 [LICENSE](LICENSE) 文件。























