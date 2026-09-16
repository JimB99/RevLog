package com.revlog.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TealPrimary = Color(0xFF1A6B7A)
private val TealDark = Color(0xFF0D4F5C)
private val TealLight = Color(0xFF4DA8B8)

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8E4EC),
    secondary = TealDark,
    tertiary = TealLight,
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color.Black,
    primaryContainer = TealDark,
    secondary = TealLight,
    tertiary = TealPrimary,
)

@Composable
fun RevLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
