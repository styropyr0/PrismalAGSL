package com.styropyr0.prismaltest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun rememberGlassPlaygroundParams(): GlassPlaygroundParams {
    val context = LocalContext.current
    val params = remember {
        GlassPlaygroundParams().also { GlassPlaygroundStorage.load(context, it) }
    }

    LaunchedEffect(params) {
        snapshotFlow { params.toSnapshot() }
            .distinctUntilChanged()
            .collect { GlassPlaygroundStorage.save(context, params) }
    }

    return params
}

private data class GlassPlaygroundSnapshot(
    val blurRadiusDp: Float,
    val refractionHeightDp: Float,
    val refractionAmountDp: Float,
    val brightness: Float,
    val saturation: Float,
    val chromaticAberration: Float,
    val cornerRadiusDp: Float,
    val depthEffect: Boolean,
    val adaptiveLuminance: Boolean,
    val useVibrancy: Boolean,
    val specularEnabled: Boolean,
    val specularAlpha: Float,
    val specularWidthDp: Float,
    val specularStyle: SpecularStyleOption,
    val depthShadowEnabled: Boolean,
    val depthShadowRadiusDp: Float,
    val depthShadowAlpha: Float,
    val depthInsetEnabled: Boolean,
    val depthInsetRadiusDp: Float,
    val depthInsetAlpha: Float,
    val surfaceTintAlpha: Float,
    val gradientBlurFadeEnd: Float,
    val gradientBottomWeight: Float,
)

private fun GlassPlaygroundParams.toSnapshot() = GlassPlaygroundSnapshot(
    blurRadiusDp = blurRadiusDp,
    refractionHeightDp = refractionHeightDp,
    refractionAmountDp = refractionAmountDp,
    brightness = brightness,
    saturation = saturation,
    chromaticAberration = chromaticAberration,
    cornerRadiusDp = cornerRadiusDp,
    depthEffect = depthEffect,
    adaptiveLuminance = adaptiveLuminance,
    useVibrancy = useVibrancy,
    specularEnabled = specularEnabled,
    specularAlpha = specularAlpha,
    specularWidthDp = specularWidthDp,
    specularStyle = specularStyle,
    depthShadowEnabled = depthShadowEnabled,
    depthShadowRadiusDp = depthShadowRadiusDp,
    depthShadowAlpha = depthShadowAlpha,
    depthInsetEnabled = depthInsetEnabled,
    depthInsetRadiusDp = depthInsetRadiusDp,
    depthInsetAlpha = depthInsetAlpha,
    surfaceTintAlpha = surfaceTintAlpha,
    gradientBlurFadeEnd = gradientBlurFadeEnd,
    gradientBottomWeight = gradientBottomWeight,
)
