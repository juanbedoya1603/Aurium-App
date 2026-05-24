package com.proyecto.aurium.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuriumColorScheme = darkColorScheme(
    primary = AuriumOrange,
    secondary = AuriumYellow,
    background = AuriumNavy,
    surface = AuriumNavy,
    onPrimary = AuriumLight,
    onSecondary = AuriumNavy,
    onBackground = AuriumLight,
    onSurface = AuriumLight
)

@Composable
fun AuriumTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuriumColorScheme,
        typography = Typography,
        content = content
    )
}