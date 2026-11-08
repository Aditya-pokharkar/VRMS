package com.thombretransport.vrms.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ThombreRed,
    onPrimary = Color.White,
    primaryContainer = ThombreRedLight,
    onPrimaryContainer = Color.White,
    secondary = ThombreRedDark,
    onSecondary = Color.White,
    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary
)

@Composable
fun VRMSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
