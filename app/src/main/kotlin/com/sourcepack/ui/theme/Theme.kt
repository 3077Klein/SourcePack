package com.sourcepack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.sourcepack.data.AppTheme

// 预定义颜色方案
private val BluePrimary = Color(0xFF2196F3)
private val BlueSecondary = Color(0xFF03A9F4)
private val BlueTertiary = Color(0xFF00BCD4)
private val BlueLightScheme = lightColorScheme(primary = BluePrimary, secondary = BlueSecondary, tertiary = BlueTertiary)
private val BlueDarkScheme = darkColorScheme(primary = BluePrimary, secondary = BlueSecondary, tertiary = BlueTertiary)

private val PurpleLightScheme = lightColorScheme(primary = Color(0xFF6750A4), secondary = Color(0xFF625B71), tertiary = Color(0xFF7D5260))
private val PurpleDarkScheme = darkColorScheme(primary = Color(0xFFD0BCFF), secondary = Color(0xFFCCC2DC), tertiary = Color(0xFFEFB8C8))

private val GrayPrimary = Color(0xFF424242)
private val GraySecondary = Color(0xFF616161)
private val GrayTertiary = Color(0xFF757575)
private val GrayLightScheme = lightColorScheme(
    primary = GrayPrimary, secondary = GraySecondary, tertiary = GrayTertiary,
    background = Color(0xFFFAFAFA), surface = Color(0xFFF5F5F5), onPrimary = Color.White
)
private val GrayDarkScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0), secondary = Color(0xFFBDBDBD), tertiary = Color(0xFF9E9E9E),
    background = Color(0xFF121212), surface = Color(0xFF1E1E1E)
)

/**
 * 应用统一主题入口
 * 支持动态取色 (Android 12+) 和自定义颜色方案切换
 */
@Composable
fun AppTheme(
    appTheme: AppTheme, 
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (appTheme) {
        AppTheme.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) PurpleDarkScheme else PurpleLightScheme
            }
        }
        AppTheme.BLUE -> if (darkTheme) BlueDarkScheme else BlueLightScheme
        AppTheme.PURPLE -> if (darkTheme) PurpleDarkScheme else PurpleLightScheme
        AppTheme.GRAY -> if (darkTheme) GrayDarkScheme else GrayLightScheme
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}