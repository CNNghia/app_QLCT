package com.app.qlct.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    secondary = ColorSecondary,
    tertiary = ColorPrimaryVariant,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = ColorPrimaryVariant,
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    secondary = ColorSecondary,
    tertiary = ColorPrimaryVariant,
    background = ColorBackground,
    surface = ColorSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextColorPrimary,
    onSurface = TextColorPrimary,
    primaryContainer = ColorPrimary,
    onPrimaryContainer = Color.White
)

@Composable
fun App_QLCTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Tắt Dynamic Color mặc định để giữ đúng thiết kế màu xanh
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // Màu nền Xanh Lá là tối, nên ta dùng Icon hệ thống (Sóng, Pin) màu Trắng (LightStatusBars = false)
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}