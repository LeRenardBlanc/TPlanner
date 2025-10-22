package com.example.tplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SteelBlue,
    secondary = LightSteelBlue,
    background = DarkGrey,
    surface = DarkGrey,
    onPrimary = DarkGrey,
    onSecondary = DarkGrey,
    onBackground = LightGrey,
    onSurface = LightGrey,
)

private val LightColorScheme = lightColorScheme(
    primary = SteelBlue,
    secondary = LightSteelBlue,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DarkGrey,
    onBackground = DarkGrey,
    onSurface = DarkGrey,
)

@Composable
fun TPlannerTheme(
    darkTheme: Boolean = true, // Force dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}