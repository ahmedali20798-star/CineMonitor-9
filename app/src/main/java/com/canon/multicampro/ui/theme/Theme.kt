package com.canon.multicampro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkStudioProColorScheme = darkColorScheme(
    primary = TelemetryCyan,
    onPrimary = StudioObsidian,
    secondary = CanonCrimson,
    onSecondary = StudioIvory,
    tertiary = StudioAmber,
    background = StudioObsidian,
    onBackground = StudioIvory,
    surface = StudioSurface,
    onSurface = StudioIvory,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = StudioSlate,
    outline = StudioBorder
)

@Composable
fun CanonMultiCamProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkStudioProColorScheme,
        typography = StudioTypography,
        content = content
    )
}
