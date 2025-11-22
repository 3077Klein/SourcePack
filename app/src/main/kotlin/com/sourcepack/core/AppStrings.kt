package com.sourcepack.core

import java.util.Locale
import com.sourcepack.BuildConfig

/**
 * 全局字符串资源管理
 * 用于在非 Android Context 环境下（如纯逻辑层）提供基础的多语言支持。
 */
object Str {
    // 检测当前系统语言，如果不是以 "en" 开头，则默认优先显示中文
    val zh = !Locale.getDefault().language.lowercase().startsWith("en")
    
    // 根据语言环境返回对应的字符串
    fun get(cn: String, en: String) = if (zh) cn else en

    val AUTHOR_NAME = "情愫 (Qingsu)"
    val AUTHOR_EMAIL = "3459162082qw@gmail.com"
    val APP_VERSION = "v${BuildConfig.VERSION_NAME}"

    val ABOUT_DESC = get(
        "SourcePack 是一款 Android 开发者工具，能将项目源码智能打包成 Markdown、XML 或纯文本，方便提交给 AI 进行分析。",
        "SourcePack is a developer tool to pack project sources into Markdown, XML, or Text for AI analysis."
    )

    val LICENSE_TEXT = """
        Copyright 2025 Qingsu

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
    """.trimIndent()
}