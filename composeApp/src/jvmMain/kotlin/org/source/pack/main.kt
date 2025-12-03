package org.source.pack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.composefluent.component.Icon
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Dismiss
import io.github.composefluent.icons.regular.Square
import io.github.composefluent.icons.regular.SquareMultiple
import io.github.composefluent.icons.regular.Subtract

// JNA Imports
import org.source.pack.jna.windows.ComposeWindowProcedure
import org.source.pack.jna.windows.structure.WinUserConst.HTCAPTION
import org.source.pack.jna.windows.structure.WinUserConst.HTCLIENT
import org.source.pack.jna.windows.structure.WinUserConst.HTCLOSE
import org.source.pack.jna.windows.structure.WinUserConst.HTMAXBUTTON
import org.source.pack.jna.windows.structure.WinUserConst.HTMINBUTTON
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import io.github.composefluent.FluentTheme

fun main() = application {
    val windowState = rememberWindowState()

    Window(
        onCloseRequest = ::exitApplication,
        title = "SourcePack",
        undecorated = true,
        transparent = false,
        state = windowState
    ) {
        val maxButtonRect = remember { mutableStateOf(Rect.Zero) }
        val minButtonRect = remember { mutableStateOf(Rect.Zero) }
        val closeButtonRect = remember { mutableStateOf(Rect.Zero) }
        val titleBarRect = remember { mutableStateOf(Rect.Zero) }

        val procedure = remember(window) {
            ComposeWindowProcedure(
                window = window,
                hitTest = { x, y ->
                    when {
                        maxButtonRect.value.contains(x, y) -> HTMAXBUTTON
                        minButtonRect.value.contains(x, y) -> HTMINBUTTON
                        closeButtonRect.value.contains(x, y) -> HTCLOSE
                        titleBarRect.value.contains(x, y) -> HTCAPTION
                        else -> HTCLIENT
                    }
                },
                onWindowInsetUpdate = { }
            )
        }
        val windowHandle = remember { procedure.windowHandle }

        // =========================================================================
        // 【关键修复】强制恢复窗口样式，启用 Snap Layouts (贴靠布局)
        // =========================================================================
        LaunchedEffect(Unit) {
            val hwnd = windowHandle
            val user32 = User32.INSTANCE

            // 1. 获取当前样式
            val oldStyle = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)

            // 2. 强制添加 WS_MAXIMIZEBOX (允许最大化) 和 WS_THICKFRAME (允许调整大小)
            // 0x00010000 = WS_MAXIMIZEBOX
            // 0x00040000 = WS_THICKFRAME
            val newStyle = oldStyle or 0x00010000 or 0x00040000
            user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, newStyle)

            // 3. 通知系统样式已更改，触发刷新
            user32.SetWindowPos(
                hwnd,
                null, 0, 0, 0, 0,
                WinUser.SWP_NOMOVE or WinUser.SWP_NOSIZE or WinUser.SWP_NOZORDER or WinUser.SWP_FRAMECHANGED
            )
        }
        // =========================================================================

        val isMaximized = windowState.placement == WindowPlacement.Maximized
        val windowBorderPadding = if (isMaximized) 8.dp else 0.dp

        Column(
            Modifier
                .fillMaxSize()
                .padding(windowBorderPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(FluentTheme.colors.background.mica.base)
                    .onGloballyPositioned { titleBarRect.value = it.boundsInWindow() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SourcePack",
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                )

                FluentCaptionButton(
                    icon = Icons.Default.Subtract,
                    contentDescription = "Minimize",
                    onClick = { User32.INSTANCE.ShowWindow(windowHandle, WinUser.SW_MINIMIZE) },
                    modifier = Modifier.onGloballyPositioned { minButtonRect.value = it.boundsInWindow() }
                )

                FluentCaptionButton(
                    icon = if (isMaximized) Icons.Default.SquareMultiple else Icons.Default.Square,
                    contentDescription = "Maximize",
                    onClick = {
                        if (isMaximized) {
                            User32.INSTANCE.ShowWindow(windowHandle, WinUser.SW_RESTORE)
                        } else {
                            User32.INSTANCE.ShowWindow(windowHandle, WinUser.SW_MAXIMIZE)
                        }
                    },
                    modifier = Modifier.onGloballyPositioned { maxButtonRect.value = it.boundsInWindow() }
                )

                FluentCaptionButton(
                    icon = Icons.Default.Dismiss,
                    contentDescription = "Close",
                    isCloseButton = true,
                    onClick = ::exitApplication,
                    modifier = Modifier.onGloballyPositioned { closeButtonRect.value = it.boundsInWindow() }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                App()
            }
        }
    }
}

@Composable
fun FluentCaptionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isCloseButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        isCloseButton && isPressed -> Color(0xFFB3261A)
        isCloseButton && isHovered -> Color(0xFFC42B1C)
        isPressed -> Color.Black.copy(alpha = 0.06f)
        isHovered -> Color.Black.copy(alpha = 0.035f)
        else -> Color.Transparent
    }

    val iconColor = when {
        isCloseButton && (isHovered || isPressed) -> Color.White
        else -> Color.Black.copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier
            .width(46.dp)
            .fillMaxHeight()
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}

fun Rect.contains(x: Float, y: Float): Boolean {
    return x >= left && x < right && y >= top && y < bottom
}