package com.steps.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = Accent, onPrimary = Color.Black, secondary = Strain,
    background = Bg, onBackground = TextPrimary, surface = Card, onSurface = TextPrimary,
    surfaceVariant = BgElevated, onSurfaceVariant = TextSecondary, outline = Stroke
)

@Composable
fun StepsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = StepsTypography, content = content)
}
