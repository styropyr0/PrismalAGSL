package com.styropyr0.prismaltest

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.PrismalGlassEffectProvider
import com.styropyr0.prismal.drawPlainPrismalGlass
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.interactive.PrismalPressRipple
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.sources.PrismalGlassLayer
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tanh

private val ModalBlurRadius = 12.dp

@Composable
fun modalSurfaceTint(): Color {
    val isLightTheme = !isSystemInDarkTheme()
    return if (isLightTheme) {
        Color.White.copy(alpha = 0.58f)
    } else {
        Color(0xFF1C1C1E).copy(alpha = 0.82f)
    }
}

fun modalGlassEffects(density: Density): PrismalGlassEffectProvider.() -> Unit = {
    applyPrismalGlassEffects(
        density = density,
        adaptiveLuminance = false,
        luminance = 0.3f,
        blurRadiusPx = with(density) { ModalBlurRadius.toPx() },
        refractionHeightPx = with(density) { 16.dp.toPx() },
        refractionAmountPx = with(density) { 32.dp.toPx() },
        useVibrancy = true,
    )
}

@Composable
fun PlaygroundGlassButton(
    onClick: () -> Unit,
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
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
            .playgroundGlassModifier(
                backdrop = backdrop,
                params = params,
                luminance = luminance,
                shape = { PrismalCapsule() },
                pressRipple = interactivePrismalSpecular,
                pressLiftPx = pressLiftPx,
                tint = tint,
                surfaceColor = surfaceColor
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

@Composable
fun PlaygroundGlassSurface(
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    shape: () -> Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    useModalMaterial: Boolean = false,
    nestedGlassSource: PrismalGlassLayer? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    val pressLiftPx = with(density) { 4.dp.toPx() }
    val resolvedSurfaceColor = when {
        surfaceColor.isSpecified -> surfaceColor
        useModalMaterial -> modalSurfaceTint()
        else -> Color.Unspecified
    }
    val animationScope = rememberCoroutineScope()
    val interactivePrismalSpecular = remember(onClick, animationScope) {
        if (onClick != null) {
            PrismalPressRipple(animationScope = animationScope)
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .playgroundGlassModifier(
                backdrop = backdrop,
                params = params,
                luminance = luminance,
                shape = shape,
                pressRipple = interactivePrismalSpecular,
                pressLiftPx = pressLiftPx,
                tint = tint,
                surfaceColor = resolvedSurfaceColor,
                useModalMaterial = useModalMaterial,
                nestedGlassSource = nestedGlassSource
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (interactivePrismalSpecular != null) {
                    Modifier
                        .then(interactivePrismalSpecular.modifier)
                        .then(interactivePrismalSpecular.gestureModifier)
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

@Composable
fun PlaygroundGlassProgressBar(
    progress: () -> Float,
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false
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
                            adaptiveLuminance = params.adaptiveLuminance,
                            luminance = luminance(),
                            blurRadiusPx = with(density) { 4.dp.toPx() },
                            refractionHeightPx = 0f,
                            refractionAmountPx = 0f,
                            useVibrancy = false,
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

@Composable
private fun Modifier.playgroundGlassModifier(
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    shape: () -> Shape,
    pressRipple: PrismalPressRipple?,
    pressLiftPx: Float,
    tint: Color,
    surfaceColor: Color,
    useModalMaterial: Boolean = false,
    nestedGlassSource: PrismalGlassLayer? = null,
): Modifier {
    val density = LocalDensity.current
    val effects =
        if (useModalMaterial) {
            modalGlassEffects(density)
        } else {
            params.glassEffects(density, luminance())
        }

    return drawPrismalGlass(
        backdrop = backdrop,
        shape = shape,
        effects = effects,
        specular = params.specularProvider(),
        depthShadow = params.depthShadowProvider(),
        depthInset = params.depthInsetProvider(),
        nestedGlassSource = nestedGlassSource,
        layerBlock = if (pressRipple != null) {
            {
                val width = size.width
                val height = size.height
                val progress = pressRipple.pressProgress
                val scale = lerp(1f, 1f + pressLiftPx / size.height, progress)

                val maxOffset = size.minDimension
                val initialDerivative = 0.05f
                val offset = pressRipple.offset
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
            if (!useModalMaterial && params.surfaceTintAlpha > 0f) {
                drawPlaygroundSurfaceTint(params)
            }
            if (tint.isSpecified) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.75f))
            }
            if (surfaceColor.isSpecified) {
                drawRect(surfaceColor)
            }
        }
    )
}

private fun DrawScope.drawPlaygroundSurfaceTint(params: GlassPlaygroundParams) {
    if (params.surfaceTintAlpha > 0f) {
        drawRect(params.surfaceTintColor())
    }
}
