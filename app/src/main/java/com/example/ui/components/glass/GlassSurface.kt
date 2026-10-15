package com.example.ui.components.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AppThemeMode
import com.example.ui.theme.LocalThemeMode

/**
 * Translucent GlassSurface for badges, inner content sections, or floating controls.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    tonalAlpha: Float = 0.08f,
    borderAlpha: Float = 0.20f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val themeMode = LocalThemeMode.current

    if (themeMode == AppThemeMode.LIQUID_GLASS) {
        val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
        val borderBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha * 1.6f),
                Color.White.copy(alpha = borderAlpha * 0.4f)
            )
        )
        val bgBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = tonalAlpha * 1.5f),
                Color(0xFF161F36).copy(alpha = 0.40f)
            )
        )

        Box(
            modifier = modifier
                .clip(shape)
                .background(bgBrush)
                .border(BorderStroke(1.dp, borderBrush), shape)
                .then(clickModifier),
            content = content
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = Color.White.copy(alpha = 0.06f),
            onClick = onClick ?: {}
        ) {
            Box(content = content)
        }
    }
}
