package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    accent: AppAccent = AppAccent.GOLD,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val appColors = remember(darkTheme, accent) {
        createAppThemeColors(isDark = darkTheme, accent = accent)
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = appColors.accent,
            onPrimary = appColors.accentInk,
            secondary = appColors.accentOrange,
            background = appColors.bg,
            surface = appColors.panel,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary
        )
        else -> lightColorScheme(
            primary = appColors.accent,
            onPrimary = appColors.accentInk,
            secondary = appColors.accentOrange,
            background = appColors.bg,
            surface = appColors.panel,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.bg.toArgb()
            window.navigationBarColor = appColors.bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
