package com.example.twofactorauth.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = SurfaceLight,
    primaryContainer = Blue80,
    secondary = Teal40,
    onSecondary = SurfaceLight,
    secondaryContainer = Teal80,
    tertiary = Amber60,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = Blue20,
    onSurface = Blue20
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue60,
    onPrimary = Blue20,
    primaryContainer = Blue20,
    secondary = Teal60,
    onSecondary = Teal20,
    secondaryContainer = Teal20,
    tertiary = Amber60,
    background = SurfaceDark,
    surface = SurfaceDark,
    onBackground = Blue80,
    onSurface = Blue80
)

@Composable
fun TwoFactorAuthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
