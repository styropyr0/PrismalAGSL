package com.styropyr0.prismal.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.interactive.PrismalPressRipple
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Capsule-shaped glass button with press ripple, scale feedback, and optional tint.
 *
 * @param onClick Click handler.
 * @param backdrop Source sampled through the glass.
 * @param isInteractive When false, disables press animations.
 * @param adaptiveLuminance When true, blur/brightness follow [luminance].
 * @param tint Optional color overlay blended onto the glass surface.
 * @param surfaceColor Optional solid fill drawn on the glass surface.
 */
@Composable
fun PrismalGlassButton(
    onClick: () -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    adaptiveLuminance: Boolean = false,
    luminance: () -> Float = { 0.5f },
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
    val pressLiftPx = with(density) { 4.dp.toPx() }
    val animationScope = rememberCoroutineScope()
    val interactivePrismalSpecular = remember(animationScope, isInteractive) {
        if (isInteractive) PrismalPressRipple(animationScope = animationScope) else null
    }

    Row(
        modifier
            .drawPrismalGlass(
                backdrop = backdrop,
                shape = { PrismalCapsule() },
                effects = {
                    applyPrismalGlassEffects(
                        density = density,
                        adaptiveLuminance = adaptiveLuminance,
                        luminance = luminance(),
                        blurRadiusPx = with(density) { 2.dp.toPx() },
                        refractionHeightPx = with(density) { 12.dp.toPx() },
                        refractionAmountPx = with(density) { 24.dp.toPx() }
                    )
                },
                layerBlock = if (interactivePrismalSpecular != null) {
                    {
                        val width = size.width
                        val height = size.height
                        val progress = interactivePrismalSpecular.pressProgress
                        val scale = lerp(1f, 1f + pressLiftPx / size.height, progress)

                        val maxOffset = size.minDimension
                        val initialDerivative = 0.05f
                        val offset = interactivePrismalSpecular.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                        val maxDragScale = pressLiftPx / size.height
                        val offsetAngle = atan2(offset.y, offset.x)
                        scaleX =
                            scale +
                                maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                (width / height).fastCoerceAtMost(1f)
                        scaleY =
                            scale +
                                maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                (height / width).fastCoerceAtMost(1f)
                    }
                } else {
                    null
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    }
                    if (surfaceColor.isSpecified) {
                        drawRect(surfaceColor)
                    }
                }
            )
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (interactivePrismalSpecular != null) {
                    Modifier
                        .then(interactivePrismalSpecular.modifier)
                        .then(interactivePrismalSpecular.gestureModifier)
                } else {
                    Modifier
                }
            )
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
