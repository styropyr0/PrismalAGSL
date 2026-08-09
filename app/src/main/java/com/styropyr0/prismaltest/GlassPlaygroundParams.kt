package com.styropyr0.prismaltest

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalGlassEffectProvider
import com.styropyr0.prismal.depth.PrismalDepthInset
import com.styropyr0.prismal.depth.PrismalDepthShadow
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.specular.PrismalSpecular
import com.styropyr0.prismal.specular.PrismalSpecularStyle

enum class SpecularStyleOption {
    Default,
    Ambient,
    Plain
}

@Stable
class GlassPlaygroundParams {
    var blurRadiusDp by mutableFloatStateOf(8f)
    var refractionHeightDp by mutableFloatStateOf(16f)
    var refractionAmountDp by mutableFloatStateOf(32f)
    var brightness by mutableFloatStateOf(0f)
    var saturation by mutableFloatStateOf(1.5f)
    var chromaticAberration by mutableFloatStateOf(0f)
    var cornerRadiusDp by mutableFloatStateOf(22f)
    var depthEffect by mutableStateOf(false)
    var adaptiveLuminance by mutableStateOf(false)
    var useVibrancy by mutableStateOf(true)
    var specularEnabled by mutableStateOf(true)
    var specularAlpha by mutableFloatStateOf(1f)
    var specularWidthDp by mutableFloatStateOf(0.5f)
    var specularStyle by mutableStateOf(SpecularStyleOption.Default)
    var depthShadowEnabled by mutableStateOf(true)
    var depthShadowRadiusDp by mutableFloatStateOf(24f)
    var depthShadowAlpha by mutableFloatStateOf(1f)
    var depthInsetEnabled by mutableStateOf(false)
    var depthInsetRadiusDp by mutableFloatStateOf(8f)
    var depthInsetAlpha by mutableFloatStateOf(0.5f)
    var surfaceTintAlpha by mutableFloatStateOf(0f)
    var gradientBlurFadeEnd by mutableFloatStateOf(0.8f)
    var gradientBottomWeight by mutableFloatStateOf(1f)

    fun reset() {
        blurRadiusDp = 8f
        refractionHeightDp = 16f
        refractionAmountDp = 32f
        brightness = 0f
        saturation = 1.5f
        chromaticAberration = 0f
        cornerRadiusDp = 22f
        depthEffect = false
        adaptiveLuminance = false
        useVibrancy = true
        specularEnabled = true
        specularAlpha = 1f
        specularWidthDp = 0.5f
        specularStyle = SpecularStyleOption.Default
        depthShadowEnabled = true
        depthShadowRadiusDp = 24f
        depthShadowAlpha = 1f
        depthInsetEnabled = false
        depthInsetRadiusDp = 8f
        depthInsetAlpha = 0.5f
        surfaceTintAlpha = 0f
        gradientBlurFadeEnd = 0.8f
        gradientBottomWeight = 1f
    }

    fun toSpecular(): PrismalSpecular {
        val style = when (specularStyle) {
            SpecularStyleOption.Default -> PrismalSpecularStyle.Default
            SpecularStyleOption.Ambient -> PrismalSpecularStyle.Ambient(intensity = specularAlpha)
            SpecularStyleOption.Plain -> PrismalSpecularStyle.Plain(
                color = Color.White.copy(alpha = 0.38f * specularAlpha)
            )
        }
        return PrismalSpecular(
            width = specularWidthDp.dp,
            blurRadius = (specularWidthDp / 2f).dp,
            alpha = specularAlpha,
            style = style
        )
    }

    fun toDepthShadow(): PrismalDepthShadow =
        PrismalDepthShadow(
            radius = depthShadowRadiusDp.dp,
            alpha = depthShadowAlpha
        )

    fun toDepthInset(): PrismalDepthInset =
        PrismalDepthInset(
            radius = depthInsetRadiusDp.dp,
            alpha = depthInsetAlpha
        )

    fun glassEffects(
        density: Density,
        luminance: Float,
    ): PrismalGlassEffectProvider.() -> Unit = {
        applyPrismalGlassEffects(
            density = density,
            adaptiveLuminance = adaptiveLuminance,
            luminance = luminance,
            blurRadiusPx = with(density) { blurRadiusDp.dp.toPx() },
            refractionHeightPx = with(density) { refractionHeightDp.dp.toPx() },
            refractionAmountPx = with(density) { refractionAmountDp.dp.toPx() },
            brightness = brightness,
            saturation = saturation,
            depthEffect = depthEffect,
            chromaticAberration = chromaticAberration,
            useVibrancy = useVibrancy
        )
    }

    fun specularProvider(): (() -> PrismalSpecular?)? =
        if (specularEnabled) ({ toSpecular() }) else null

    fun depthShadowProvider(): (() -> PrismalDepthShadow?)? =
        if (depthShadowEnabled) ({ toDepthShadow() }) else null

    fun depthInsetProvider(): (() -> PrismalDepthInset?)? =
        if (depthInsetEnabled) ({ toDepthInset() }) else null

    fun surfaceTintColor(): Color =
        Color.White.copy(alpha = surfaceTintAlpha)
}
