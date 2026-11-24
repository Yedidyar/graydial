package com.graydial.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// A strictly neutral palette: every colour has R == G == B.
private val Ink = Color(0xFF111111)
private val Graphite = Color(0xFF2B2B2B)
private val Slate = Color(0xFF5C5C5C)
private val Ash = Color(0xFF9A9A9A)
private val Mist = Color(0xFFE4E4E4)
private val Paper = Color(0xFFF7F7F7)
private val Snow = Color(0xFFFFFFFF)
private val Night = Color(0xFF0B0B0B)
private val Coal = Color(0xFF1A1A1A)

private val LightColors = lightColorScheme(
    primary = Ink, onPrimary = Snow,
    primaryContainer = Mist, onPrimaryContainer = Ink,
    secondary = Slate, onSecondary = Snow,
    secondaryContainer = Mist, onSecondaryContainer = Ink,
    background = Paper, onBackground = Ink,
    surface = Paper, onSurface = Ink,
    surfaceVariant = Mist, onSurfaceVariant = Slate,
    surfaceContainer = Snow, surfaceContainerHigh = Mist, surfaceContainerHighest = Mist,
    outline = Ash, outlineVariant = Mist,
    error = Graphite, onError = Snow,
    errorContainer = Mist, onErrorContainer = Ink,
)

private val DarkColors = darkColorScheme(
    primary = Mist, onPrimary = Night,
    primaryContainer = Graphite, onPrimaryContainer = Snow,
    secondary = Ash, onSecondary = Night,
    secondaryContainer = Graphite, onSecondaryContainer = Snow,
    background = Night, onBackground = Mist,
    surface = Night, onSurface = Mist,
    surfaceVariant = Graphite, onSurfaceVariant = Ash,
    surfaceContainer = Coal, surfaceContainerHigh = Graphite, surfaceContainerHighest = Graphite,
    outline = Slate, outlineVariant = Graphite,
    error = Mist, onError = Night,
    errorContainer = Graphite, onErrorContainer = Snow,
)

private val GrayDialTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontWeight = FontWeight.Light, fontSize = 88.sp, letterSpacing = (-2).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
        labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp),
    )
}

@Composable
fun GrayDialTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = GrayDialTypography,
        content = content,
    )
}
