package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeMode

val LocalThemeMode = staticCompositionLocalOf { AppThemeMode.DARK }

private val DarkColorScheme = darkColorScheme(
    primary = PulseBlue,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = PulseCyan,
    secondary = PulseCyan,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = PulseCyan,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    error = StatusRed
)

private val LightColorScheme = lightColorScheme(
    primary = LightPulseBlue,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = LightPulseBlue,
    secondary = LightPulseCyan,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceElevated,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightPulseCyan,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    error = StatusRed
)

private val LiquidGlassColorScheme = darkColorScheme(
    primary = PulseBlue,
    onPrimary = Color.White,
    primaryContainer = LiquidGlassSurfaceElevated,
    onPrimaryContainer = PulseCyan,
    secondary = PulseCyan,
    onSecondary = Color.Black,
    secondaryContainer = LiquidGlassSurfaceElevated,
    onSecondaryContainer = LiquidGlassTextPrimary,
    tertiary = PulseCyan,
    background = LiquidGlassDarkBg,
    onBackground = LiquidGlassTextPrimary,
    surface = LiquidGlassSurface,
    onSurface = LiquidGlassTextPrimary,
    surfaceVariant = LiquidGlassSurfaceElevated,
    onSurfaceVariant = LiquidGlassTextSecondary,
    outline = LiquidGlassBorder,
    error = StatusRed
)

@Composable
fun BaseusPulseTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.LIQUID_GLASS -> LiquidGlassColorScheme
    }

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun BaseusPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    BaseusPulseTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BaseusPulseTheme(darkTheme = darkTheme, content = content)
}
