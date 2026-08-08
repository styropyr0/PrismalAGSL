package com.styropyr0.prismaltest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle

@Composable
fun TunableGlassPanel(
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier.drawPrismalGlass(
            backdrop = backdrop,
            shape = { PrismalRoundedRectangle(cornerRadius) },
            effects = {
                applyPrismalGlassEffects(
                    density = density,
                    adaptiveLuminance = params.adaptiveLuminance,
                    luminance = luminance(),
                    blurRadiusPx = with(density) { params.blurRadiusDp.dp.toPx() },
                    refractionHeightPx = with(density) { params.refractionHeightDp.dp.toPx() },
                    refractionAmountPx = with(density) { params.refractionAmountDp.dp.toPx() },
                    brightness = params.brightness,
                    saturation = params.saturation,
                    depthEffect = params.depthEffect,
                    chromaticAberration = params.chromaticAberration,
                    useVibrancy = params.useVibrancy
                )
            },
            specular = if (params.specularEnabled) {
                { params.toSpecular() }
            } else {
                null
            },
            depthShadow = if (params.depthShadowEnabled) {
                { params.toDepthShadow() }
            } else {
                null
            },
            depthInset = if (params.depthInsetEnabled) {
                { params.toDepthInset() }
            } else {
                null
            },
            onDrawSurface = {
                if (params.surfaceTintAlpha > 0f) {
                    drawRect(Color.White.copy(alpha = params.surfaceTintAlpha))
                }
            }
        ),
        content = content
    )
}
