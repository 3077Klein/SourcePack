package org.source.pack.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import io.github.composefluent.FluentTheme
import org.source.pack.jna.windows.structure.isWindows10OrLater
import org.jetbrains.skiko.hostOs

@Composable
fun FrameWindowScope.WindowFrame(
    onCloseRequest: () -> Unit,
    icon: Painter? = null,
    title: String = "",
    state: WindowState,
    backButtonVisible: Boolean = true,
    backButtonEnabled: Boolean = false,
    backButtonClick: () -> Unit = {},
    captionBarHeight: Dp = 48.dp,
    content: @Composable (windowInset: WindowInsets, captionBarInset: WindowInsets) -> Unit
) {
    // 1. 移除 GalleryTheme，改用通用的 FluentTheme
    // 2. 移除了 LocalStore 和 NavigationDisplayMode 的逻辑，这些是原应用的业务逻辑
    FluentTheme {
        when {
            // Windows 10/11 使用自定义的无边框窗口
            hostOs.isWindows && isWindows10OrLater() -> {
                WindowsWindowFrame(
                    onCloseRequest = onCloseRequest,
                    icon = icon,
                    title = title,
                    content = content,
                    state = state,
                    backButtonVisible = backButtonVisible,
                    backButtonEnabled = backButtonEnabled,
                    backButtonClick = backButtonClick,
                    captionBarHeight = captionBarHeight
                )
            }

            // MacOS 部分如果没有复制对应的文件，建议先注释掉或直接运行默认内容
            /*
            hostOs.isMacOS -> {
                MacOSWindowFrame(...)
            }
            */

            // 其他系统或低版本 Windows 使用默认样式
            else -> {
                content(WindowInsets(0), WindowInsets(0))
            }
        }
    }
}