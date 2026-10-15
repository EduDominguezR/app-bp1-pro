package com.example.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeMode
import com.example.ui.theme.LiquidGlassDarkBg
import com.example.ui.theme.LiquidGlassGlowBlue
import com.example.ui.theme.LiquidGlassGlowCyan
import com.example.ui.theme.LocalThemeMode

/**
 * Ambient background container for Liquid Glass aesthetic.
 * When Liquid Glass is active, draws a deep obsidian base with static
 * soft-glow ambient mesh orbs in the background to provide rich depth
 * for translucent glass cards, with zero battery drain or frame drops.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val themeMode = LocalThemeMode.current

    val backgroundModifier = if (themeMode == AppThemeMode.LIQUID_GLASS) {
        Modifier
            .fillMaxSize()
            .drawBehind {
                val width = size.width
                val height = size.height

                // Base deep midnight obsidian
                drawRect(color = Color(0xFF070B16))

                // Hero backdrop halo (subtle back-light behind headphones)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidGlassGlowBlue.copy(alpha = 0.50f),
                            LiquidGlassGlowBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.50f, height * 0.25f),
                        radius = width * 0.65f
                    ),
                    center = Offset(width * 0.50f, height * 0.25f),
                    radius = width * 0.65f
                )

                // Top-right soft electric blue ambient orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF264FC4).copy(alpha = 0.32f),
                            Color(0xFF1D357A).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.90f, height * 0.10f),
                        radius = width * 0.70f
                    ),
                    center = Offset(width * 0.90f, height * 0.10f),
                    radius = width * 0.70f
                )

                // Center-left soft cyan ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidGlassGlowCyan.copy(alpha = 0.38f),
                            LiquidGlassGlowCyan.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.10f, height * 0.60f),
                        radius = width * 0.65f
                    ),
                    center = Offset(width * 0.10f, height * 0.60f),
                    radius = width * 0.65f
                )

                // Bottom ambient indigo glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF231E54).copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.75f, height * 0.88f),
                        radius = width * 0.55f
                    ),
                    center = Offset(width * 0.75f, height * 0.88f),
                    radius = width * 0.55f
                )

                // Subtle dark vignette gradient across top and bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF070B16).copy(alpha = 0.40f),
                            Color.Transparent,
                            Color(0xFF070B16).copy(alpha = 0.70f)
                        )
                    )
                )
            }
    } else {
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    }

    Box(
        modifier = modifier.then(backgroundModifier),
        content = content
    )
}
