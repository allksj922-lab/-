package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SeniorIvoryColorScheme = lightColorScheme(
    primary = EmeraldDoneContainer,
    onPrimary = WarmIvoryCard,
    primaryContainer = EmeraldDoneLightBg,
    onPrimaryContainer = EmeraldDoneText,
    secondary = OceanBluePrimary,
    onSecondary = WarmIvoryCard,
    secondaryContainer = WaterCardContainer,
    onSecondaryContainer = OceanBluePrimary,
    background = WarmIvoryBackground,
    onBackground = DeepCharcoalText,
    surface = WarmIvoryCard,
    onSurface = DeepCharcoalText,
    surfaceVariant = WarmIvorySubtle,
    onSurfaceVariant = WarmSlateText,
    outline = WarmStoneBorder
)

@Composable
fun SeniorCareTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SeniorIvoryColorScheme,
        typography = SeniorTypography,
        content = content
    )
}
