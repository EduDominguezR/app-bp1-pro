package com.example.ui.components.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AppThemeMode
import com.example.ui.theme.LocalThemeMode

/**
 * Reusable GlassCard that automatically renders frosted translucent glass
 * with specular beveled borders in Liquid Glass mode, while rendering standard
 * Material 3 surface elevation in Dark and Light themes.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 6.dp,
    borderAlpha: Float = 0.22f,
    fillAlpha: Float = 0.08f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val themeMode = LocalThemeMode.current

    if (themeMode == AppThemeMode.LIQUID_GLASS) {
        // Directional specular fill (overhead light angle)
        val glassBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = fillAlpha * 1.8f),
                Color(0xFF141C30).copy(alpha = 0.50f),
                Color(0xFF0C1222).copy(alpha = 0.65f)
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )

        // Specular rim border: bright overhead highlight fading to darker bottom rim
        val borderBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha * 1.8f),
                Color.White.copy(alpha = borderAlpha * 0.75f),
                Color.White.copy(alpha = borderAlpha * 0.25f)
            )
        )

        val clickModifier = if (onClick != null) {
            Modifier.clickable { onClick() }
        } else {
            Modifier
        }

        Box(
            modifier = modifier
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = Color.Black.copy(alpha = 0.55f),
                    ambientColor = Color(0xFF0A0F1D).copy(alpha = 0.35f)
                )
                .clip(shape)
                .background(glassBrush)
                .border(BorderStroke(1.dp, borderBrush), shape)
                .then(clickModifier)
        ) {
            Column(content = content)
        }
    } else {
        // Dark or Light theme standard elevation
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            onClick = onClick ?: {}
        ) {
            Column(content = content)
        }
    }
}
