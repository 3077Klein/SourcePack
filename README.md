### English
SourcePack is an Android utility designed to consolidate project source code into a single structured file (Markdown, XML, or Plain Text). It facilitates the export of codebases for analysis by Large Language Models (LLMs) or for documentation purposes.
Key Features
 * Source Consolidation: Recursively scans local directories to generate a unified output file containing both the directory structure and file contents.
 * GitHub Integration: Accepts GitHub repository URLs to download the HEAD branch as a ZIP archive. The file tree is processed entirely in-memory, eliminating the need for a local Git client.
 * Structural Context: Generates a complete directory tree visualization at the beginning of the output. Files excluded by filters (e.g., binaries) remain visible in the tree to preserve architectural context for the analyzer.
 * Smart Filtering:
   * System Ignores: Automatically excludes build artifacts and metadata directories (e.g., .git, .gradle, build, node_modules).
   * Binary Detection: Skips binary files based on extension and content analysis to reduce token usage.
   * Custom Rules: Supports user-defined blacklists for specific filenames and extensions.
 * Output Formats:
   * Markdown: Encapsulates code in language-specific triple-backtick blocks.
   * XML: Wraps content in hierarchical tags for structured parsing.
   * Plain Text: Uses simple delimiters.
 * Compression: Optional mode to strip excess whitespace and newlines.
Technical Stack
 * Language: Kotlin 2.0
 * UI Framework: Jetpack Compose (Material 3)
 * Architecture: MVVM with Coroutines and Flow
 * Minimum SDK: Android 7.0 (API 24)
Usage
 * Select Source:
   * Folder: Grant access to a local project directory via the system document picker.
   * Files: Select specific multiple files.
   * GitHub: Input a repository URL (e.g., https://github.com/username/repo).
 * Configuration: Access settings to toggle compression, select output format (MD/XML/TXT), or modify exclusion rules.
 * Export: The application processes the input and writes the result to a user-selected destination URI.
Build Instructions
To build the release APK:
./gradlew assembleRelease

License
Copyright 2025 Qingsu.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## 中文

SourcePack 是一款 Android 开发者工具，用于将源代码项目整合为单一的 Markdown、XML 或文本文件。该格式专为 ChatGPT、Claude 或 Gemini 等大语言模型优化，便于 AI 高效分析项目结构与逻辑。

### 功能特点

*   **完整项目扫描**：递归扫描本地文件夹，生成完整的文件树结构。
*   **GitHub 集成**：支持直接输入 GitHub 仓库地址，自动下载 `HEAD` 分支并在内存中处理，无需本地 Git 客户端。
*   **架构可见性**：即使文件内容被忽略（如图片、二进制文件），文件树中仍会保留其文件名，确保 AI 能够理解完整的项目架构。
*   **智能过滤**：自动忽略系统目录（如 `.git`, `build`, `node_modules`）及二进制文件，节省 Token 用量。
*   **自定义规则**：支持用户自定义文件名或后缀黑名单。
*   **多格式输出**：支持 Markdown（代码块高亮）、XML（结构化数据）及纯文本。

### 使用指南

1.  **选择来源**：
    *   **文件夹**：选择本地项目目录。
    *   **文件**：多选特定文件。
    *   **GitHub**：输入仓库链接（例如 `https://github.com/username/repo`）。
2.  **配置**：
    *   在设置中调整过滤规则。
    *   选择输出格式（推荐使用 MD 格式以获得最佳 AI 阅读效果）。
3.  **导出**：
    *   应用将生成 `.md` 或 `.xml` 文件。
    *   将文件上传至 AI 对话界面即可开始分析。

### 构建

环境要求：Android Studio Ladybug 或更新版本，JDK 17+。

```bash
git clone https://github.com/yourusername/SourcePack.git
cd SourcePack
./gradlew assembleRelease

### 开源许可

Copyright 2025 Qingsu.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.