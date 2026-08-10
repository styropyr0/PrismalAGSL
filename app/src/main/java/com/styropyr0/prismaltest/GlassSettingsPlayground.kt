package com.styropyr0.prismaltest

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.PrismalGlass
import com.styropyr0.prismal.effects.rememberPrismalAdaptiveLuminance
import com.styropyr0.prismal.sources.PrismalGlassLayer

@Composable
fun GlassSettingsPlayground(
    backdrop: PrismalBackdrop,
    backdropLayer: PrismalGlassLayer,
    params: GlassPlaygroundParams,
    onPickWallpaper: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isLightTheme = !isSystemInDarkTheme()
    val adaptiveLuminance = rememberPrismalAdaptiveLuminance(
        enabled = params.adaptiveLuminance,
        source = backdropLayer,
        isLightTheme = isLightTheme
    )
    val luminance = { adaptiveLuminance.luminance }

    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item {
            IosLargeTitle(title = "Settings")
        }

        item {
            IosSectionHeader("Appearance")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosListRow(
                    title = "Wallpaper",
                    value = "Change",
                    showChevron = true,
                    onClick = onPickWallpaper
                )
            }
            IosSectionFooter("Live preview of the current glass configuration.")
        }

        item {
            IosSectionHeader("Platform")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosListRow(
                    title = "Pipeline",
                    value = PrismalGlass.pipeline.name
                )
            }
        }

        item {
            IosSectionHeader("Refraction & Blur")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosSliderRow(
                    title = "Blur Radius",
                    value = params.blurRadiusDp,
                    valueLabel = "${params.blurRadiusDp.toInt()} pt",
                    onValueChange = { params.blurRadiusDp = it },
                    valueRange = 0f..24f,
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosSliderRow(
                    title = "Refraction Height",
                    value = params.refractionHeightDp,
                    valueLabel = "${params.refractionHeightDp.toInt()} pt",
                    onValueChange = { params.refractionHeightDp = it },
                    valueRange = 0f..32f,
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosSliderRow(
                    title = "Refraction Amount",
                    value = params.refractionAmountDp,
                    valueLabel = "${params.refractionAmountDp.toInt()} pt",
                    onValueChange = { params.refractionAmountDp = it },
                    valueRange = 0f..60f,
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosSliderRow(
                    title = "Chromatic Aberration",
                    value = params.chromaticAberration,
                    valueLabel = "${(params.chromaticAberration * 100).toInt()}%",
                    onValueChange = { params.chromaticAberration = it },
                    valueRange = 0f..1f,
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosToggleRow(
                    title = "Depth Effect",
                    checked = params.depthEffect,
                    onCheckedChange = { params.depthEffect = it },
                    backdrop = backdrop
                )
            }
        }

        item {
            IosSectionHeader("Shape")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosSliderRow(
                    title = "Corner Radius",
                    value = params.cornerRadiusDp,
                    valueLabel = "${params.cornerRadiusDp.toInt()} pt",
                    onValueChange = { params.cornerRadiusDp = it },
                    valueRange = 0f..48f,
                    backdrop = backdrop
                )
            }
        }

        item {
            IosSectionHeader("Color & Luminance")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosToggleRow(
                    title = "Adaptive Luminance",
                    checked = params.adaptiveLuminance,
                    onCheckedChange = { params.adaptiveLuminance = it },
                    backdrop = backdrop
                )
                if (!params.adaptiveLuminance) {
                    IosGroupDivider()
                    IosToggleRow(
                        title = "Vibrancy",
                        checked = params.useVibrancy,
                        onCheckedChange = { params.useVibrancy = it },
                        backdrop = backdrop
                    )
                }
                IosGroupDivider()
                IosSliderRow(
                    title = "Brightness",
                    value = params.brightness,
                    valueLabel = "%.2f".format(params.brightness),
                    onValueChange = {
                        params.brightness = it
                        if (params.useVibrancy) {
                            params.useVibrancy = false
                        }
                    },
                    valueRange = -0.3f..0.5f,
                    backdrop = backdrop,
                    enabled = !params.adaptiveLuminance
                )
                if (!params.adaptiveLuminance && !params.useVibrancy) {
                    IosGroupDivider()
                    IosSliderRow(
                        title = "Saturation",
                        value = params.saturation,
                        valueLabel = "%.2f".format(params.saturation),
                        onValueChange = { params.saturation = it },
                        valueRange = 0.5f..2.5f,
                        backdrop = backdrop
                    )
                }
                IosGroupDivider()
                IosSliderRow(
                    title = "Surface Tint",
                    value = params.surfaceTintAlpha,
                    valueLabel = "${(params.surfaceTintAlpha * 100).toInt()}%",
                    onValueChange = { params.surfaceTintAlpha = it },
                    valueRange = 0f..0.6f,
                    backdrop = backdrop
                )
            }
            if (params.adaptiveLuminance) {
                IosSectionFooter("Turn off Adaptive Luminance to tune brightness manually.")
            } else if (params.useVibrancy) {
                IosSectionFooter("Adjusting brightness switches to manual color controls.")
            }
        }

        item {
            IosSectionHeader("Specular Highlight")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosToggleRow(
                    title = "Enabled",
                    checked = params.specularEnabled,
                    onCheckedChange = { params.specularEnabled = it },
                    backdrop = backdrop
                )
                if (params.specularEnabled) {
                    IosGroupDivider()
                    IosGlassDropdownRow(
                        title = "Style",
                        options = SpecularStyleOption.entries.map { it.name },
                        selectedIndex = SpecularStyleOption.entries.indexOf(params.specularStyle),
                        onSelected = { params.specularStyle = SpecularStyleOption.entries[it] },
                        backdrop = backdrop,
                        params = params,
                        luminance = luminance,
                    )
                    IosGroupDivider()
                    IosSliderRow(
                        title = "Alpha",
                        value = params.specularAlpha,
                        valueLabel = "${(params.specularAlpha * 100).toInt()}%",
                        onValueChange = { params.specularAlpha = it },
                        valueRange = 0f..1f,
                        backdrop = backdrop
                    )
                    if (params.specularStyle != SpecularStyleOption.Ambient) {
                        IosGroupDivider()
                        IosSliderRow(
                            title = "Width",
                            value = params.specularWidthDp,
                            valueLabel = "%.2f pt".format(params.specularWidthDp),
                            onValueChange = { params.specularWidthDp = it },
                            valueRange = 0.25f..2f,
                            backdrop = backdrop
                        )
                    }
                }
            }
        }

        item {
            IosSectionHeader("Depth")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosToggleRow(
                    title = "Drop Shadow",
                    checked = params.depthShadowEnabled,
                    onCheckedChange = { params.depthShadowEnabled = it },
                    backdrop = backdrop
                )
                if (params.depthShadowEnabled) {
                    IosGroupDivider()
                    IosSliderRow(
                        title = "Shadow Radius",
                        value = params.depthShadowRadiusDp,
                        valueLabel = "${params.depthShadowRadiusDp.toInt()} pt",
                        onValueChange = { params.depthShadowRadiusDp = it },
                        valueRange = 0f..32f,
                        backdrop = backdrop
                    )
                    IosGroupDivider()
                    IosSliderRow(
                        title = "Shadow Alpha",
                        value = params.depthShadowAlpha,
                        valueLabel = "${(params.depthShadowAlpha * 100).toInt()}%",
                        onValueChange = { params.depthShadowAlpha = it },
                        valueRange = 0f..1f,
                        backdrop = backdrop
                    )
                }
                IosGroupDivider()
                IosToggleRow(
                    title = "Depth Inset",
                    checked = params.depthInsetEnabled,
                    onCheckedChange = { params.depthInsetEnabled = it },
                    backdrop = backdrop
                )
                if (params.depthInsetEnabled) {
                    IosGroupDivider()
                    IosSliderRow(
                        title = "Inset Radius",
                        value = params.depthInsetRadiusDp,
                        valueLabel = "${params.depthInsetRadiusDp.toInt()} pt",
                        onValueChange = { params.depthInsetRadiusDp = it },
                        valueRange = 0f..24f,
                        backdrop = backdrop
                    )
                    IosGroupDivider()
                    IosSliderRow(
                        title = "Inset Alpha",
                        value = params.depthInsetAlpha,
                        valueLabel = "${(params.depthInsetAlpha * 100).toInt()}%",
                        onValueChange = { params.depthInsetAlpha = it },
                        valueRange = 0f..1f,
                        backdrop = backdrop
                    )
                }
            }
        }

        item {
            IosSectionHeader("Gradient Glass")
            IosGlassGroup(backdrop = backdrop, params = params, luminance = luminance) {
                IosSliderRow(
                    title = "Bottom Refraction Weight",
                    value = params.gradientBottomWeight,
                    valueLabel = "%.2f".format(params.gradientBottomWeight),
                    onValueChange = { params.gradientBottomWeight = it },
                    valueRange = 0f..1f,
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosSliderRow(
                    title = "Blur Fade End",
                    value = params.gradientBlurFadeEnd,
                    valueLabel = "%.2f".format(params.gradientBlurFadeEnd),
                    onValueChange = { params.gradientBlurFadeEnd = it },
                    valueRange = 0f..1f,
                    backdrop = backdrop
                )
            }
        }

        item {
            IosDestructiveButton(
                text = "Reset All Settings",
                onClick = { params.reset() },
                backdrop = backdrop,
                params = params,
                luminance = luminance
            )
            IosSectionFooter("Restores the default glass preset used across the app.")
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
