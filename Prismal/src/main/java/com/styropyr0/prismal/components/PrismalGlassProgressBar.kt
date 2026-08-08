package com.styropyr0.prismal.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import kotlin.math.min
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPlainPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.shapes.PrismalCapsule

/**
 * Single-layer glass progress track. Fill is drawn on [onDrawSurface] to avoid nested
 * backdrop chains that overflow the RenderThread stack.
 *
 * @param progress Current progress in `[0, 1]`; ignored when [indeterminate] is true.
 * @param indeterminate When true, shows a looping animation instead of [progress].
 */
@Composable
fun PrismalGlassProgressBar(
    progress: () -> Float,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    adaptiveLuminance: Boolean = false,
    luminance: () -> Float = { 0.5f }
) {
    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor =
        if (isLightTheme) Color(0xFF0088FF)
        else Color(0xFF0091FF)
    val trackColor =
        if (isLightTheme) Color(0xFF787878).copy(0.2f)
        else Color(0xFF787880).copy(0.36f)

    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    val indeterminateShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shift"
    )

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val fraction = if (indeterminate) 0.35f else progress().fastCoerceIn(0f, 1f)

        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .drawPlainPrismalGlass(
                    backdrop = backdrop,
                    shape = { PrismalCapsule() },
                    effects = {
                        applyPrismalGlassEffects(
                            density = density,
                            adaptiveLuminance = adaptiveLuminance,
                            luminance = luminance(),
                            blurRadiusPx = with(density) { 4.dp.toPx() },
                            refractionHeightPx = with(density) { 8.dp.toPx() },
                            refractionAmountPx = with(density) { 12.dp.toPx() }
                        )
                    },
                    onDrawSurface = {
                        drawRect(trackColor)
                        if (indeterminate) {
                            val fillWidth = size.width * fraction
                            val x = (size.width + fillWidth) * indeterminateShift - fillWidth
                            drawRoundRect(
                                color = accentColor.copy(alpha = 0.85f),
                                topLeft = Offset(x, 0f),
                                size = Size(fillWidth, size.height),
                                cornerRadius = CornerRadius(
                                    min(size.height / 2f, fillWidth / 2f)
                                )
                            )
                        } else {
                            val fillWidth = size.width * fraction
                            if (fillWidth > 0f) {
                                drawRoundRect(
                                    color = accentColor.copy(alpha = 0.85f),
                                    size = Size(fillWidth, size.height),
                                    cornerRadius = CornerRadius(
                                        min(size.height / 2f, fillWidth / 2f)
                                    )
                                )
                            }
                        }
                    }
                )
        )
    }
}
