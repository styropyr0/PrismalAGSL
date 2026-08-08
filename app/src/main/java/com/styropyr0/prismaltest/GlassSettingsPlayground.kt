package com.styropyr0.prismaltest

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.PrismalGlass
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalGlassToggle
import com.styropyr0.prismal.components.PrismalGradientGlassPanel
import com.styropyr0.prismal.effects.rememberPrismalAdaptiveLuminance
import com.styropyr0.prismal.sources.PrismalGlassLayer

@Composable
fun GlassSettingsPlayground(
    backdrop: PrismalBackdrop,
    backdropLayer: PrismalGlassLayer,
    params: GlassPlaygroundParams,
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Glass Playground",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tune liquid glass parameters and preview changes live.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Pipeline: ${PrismalGlass.pipeline.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )
            }
        }

        item {
            PlaygroundSection(title = "Live Preview") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TunableGlassPanel(
                        backdrop = backdrop,
                        params = params,
                        luminance = luminance,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                    PrismalGradientGlassPanel(
                        backdrop = backdrop,
                        height = 120.dp,
                        adaptiveLuminance = params.adaptiveLuminance,
                        luminance = luminance,
                        blurRadiusDp = params.blurRadiusDp.dp,
                        refractionHeightDp = params.refractionHeightDp.dp,
                        refractionAmountDp = params.refractionAmountDp.dp,
                        refractionBottomWeight = params.gradientBottomWeight,
                        blurFadeEnd = params.gradientBlurFadeEnd,
                        chromaticAberration = params.chromaticAberration
                    )
                }
            }
        }

        item {
            PlaygroundSection(title = "Refraction & Blur") {
                PlaygroundSlider(
                    label = "Blur radius",
                    value = params.blurRadiusDp,
                    valueRange = 0f..24f,
                    valueLabel = { "${it.toInt()} dp" },
                    onValueChange = { params.blurRadiusDp = it },
                    backdrop = backdrop,
                )
                PlaygroundSlider(
                    label = "Refraction height",
                    value = params.refractionHeightDp,
                    valueRange = 0f..32f,
                    valueLabel = { "${it.toInt()} dp" },
                    onValueChange = { params.refractionHeightDp = it },
                    backdrop = backdrop,
                )
                PlaygroundSlider(
                    label = "Refraction amount",
                    value = params.refractionAmountDp,
                    valueRange = 0f..48f,
                    valueLabel = { "${it.toInt()} dp" },
                    onValueChange = { params.refractionAmountDp = it },
                    backdrop = backdrop,
                )
                PlaygroundSwitch(
                    label = "Chromatic aberration",
                    checked = params.chromaticAberration,
                    onCheckedChange = { params.chromaticAberration = it },
                    backdrop = backdrop,
                )
                PlaygroundSwitch(
                    label = "Depth effect",
                    checked = params.depthEffect,
                    onCheckedChange = { params.depthEffect = it },
                    backdrop = backdrop,
                )
            }
        }

        item {
            PlaygroundSection(title = "Color & Luminance") {
                PlaygroundSwitch(
                    label = "Adaptive luminance",
                    checked = params.adaptiveLuminance,
                    onCheckedChange = { params.adaptiveLuminance = it },
                    backdrop = backdrop,
                )
                if (!params.adaptiveLuminance) {
                    PlaygroundSwitch(
                        label = "Vibrancy",
                        checked = params.useVibrancy,
                        onCheckedChange = { params.useVibrancy = it },
                    backdrop = backdrop,
                )
                    if (!params.useVibrancy) {
                        PlaygroundSlider(
                            label = "Brightness",
                            value = params.brightness,
                            valueRange = -0.3f..0.5f,
                            valueLabel = { "%.2f".format(it) },
                            onValueChange = { params.brightness = it },
                    backdrop = backdrop,
                )
                        PlaygroundSlider(
                            label = "Saturation",
                            value = params.saturation,
                            valueRange = 0.5f..2.5f,
                            valueLabel = { "%.2f".format(it) },
                            onValueChange = { params.saturation = it },
                    backdrop = backdrop,
                )
                    }
                }
                PlaygroundSlider(
                    label = "Surface tint",
                    value = params.surfaceTintAlpha,
                    valueRange = 0f..0.6f,
                    valueLabel = { "%.0f%%".format(it * 100) },
                    onValueChange = { params.surfaceTintAlpha = it },
                    backdrop = backdrop,
                )
            }
        }

        item {
            PlaygroundSection(title = "Specular Highlight") {
                PlaygroundSwitch(
                    label = "Enabled",
                    checked = params.specularEnabled,
                    onCheckedChange = { params.specularEnabled = it },
                    backdrop = backdrop,
                )
                if (params.specularEnabled) {
                    PlaygroundEnumChips(
                        label = "Style",
                        backdrop = backdrop,
                        params = params,
                        luminance = luminance,
                        options = SpecularStyleOption.entries,
                        selected = params.specularStyle,
                        onSelected = { params.specularStyle = it },
                        labelFor = { it.name }
                    )
                    PlaygroundSlider(
                        label = "Alpha",
                        value = params.specularAlpha,
                        valueRange = 0f..1f,
                        valueLabel = { "%.0f%%".format(it * 100) },
                        onValueChange = { params.specularAlpha = it },
                    backdrop = backdrop,
                )
                    if (params.specularStyle != SpecularStyleOption.Ambient) {
                        PlaygroundSlider(
                            label = "Width",
                            value = params.specularWidthDp,
                            valueRange = 0.25f..2f,
                            valueLabel = { "%.2f dp".format(it) },
                            onValueChange = { params.specularWidthDp = it },
                    backdrop = backdrop,
                )
                    }
                }
            }
        }

        item {
            PlaygroundSection(title = "Depth") {
                PlaygroundSwitch(
                    label = "Drop shadow",
                    checked = params.depthShadowEnabled,
                    onCheckedChange = { params.depthShadowEnabled = it },
                    backdrop = backdrop,
                )
                if (params.depthShadowEnabled) {
                    PlaygroundSlider(
                        label = "Shadow radius",
                        value = params.depthShadowRadiusDp,
                        valueRange = 0f..32f,
                        valueLabel = { "${it.toInt()} dp" },
                        onValueChange = { params.depthShadowRadiusDp = it },
                    backdrop = backdrop,
                )
                    PlaygroundSlider(
                        label = "Shadow alpha",
                        value = params.depthShadowAlpha,
                        valueRange = 0f..1f,
                        valueLabel = { "%.0f%%".format(it * 100) },
                        onValueChange = { params.depthShadowAlpha = it },
                    backdrop = backdrop,
                )
                }
                PlaygroundSwitch(
                    label = "Depth inset",
                    checked = params.depthInsetEnabled,
                    onCheckedChange = { params.depthInsetEnabled = it },
                    backdrop = backdrop,
                )
                if (params.depthInsetEnabled) {
                    PlaygroundSlider(
                        label = "Inset radius",
                        value = params.depthInsetRadiusDp,
                        valueRange = 0f..24f,
                        valueLabel = { "${it.toInt()} dp" },
                        onValueChange = { params.depthInsetRadiusDp = it },
                    backdrop = backdrop,
                )
                    PlaygroundSlider(
                        label = "Inset alpha",
                        value = params.depthInsetAlpha,
                        valueRange = 0f..1f,
                        valueLabel = { "%.0f%%".format(it * 100) },
                        onValueChange = { params.depthInsetAlpha = it },
                    backdrop = backdrop,
                )
                }
            }
        }

        item {
            PlaygroundSection(title = "Gradient Glass") {
                PlaygroundSlider(
                    label = "Bottom refraction weight",
                    value = params.gradientBottomWeight,
                    valueRange = 0f..1f,
                    valueLabel = { "%.2f".format(it) },
                    onValueChange = { params.gradientBottomWeight = it },
                    backdrop = backdrop,
                )
                PlaygroundSlider(
                    label = "Blur fade end",
                    value = params.gradientBlurFadeEnd,
                    valueRange = 0f..1f,
                    valueLabel = { "%.2f".format(it) },
                    onValueChange = { params.gradientBlurFadeEnd = it },
                    backdrop = backdrop,
                )
            }
        }

        item {
            PlaygroundGlassButton(
                onClick = { params.reset() },
                backdrop = backdrop,
                params = params,
                luminance = luminance,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to defaults")
            }
        }
    }
}

@Composable
private fun PlaygroundSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun PlaygroundSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: (Float) -> String,
    onValueChange: (Float) -> Unit,
    backdrop: PrismalBackdrop,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                valueLabel(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        PrismalGlassSlider(
            value = { value },
            onValueChange = onValueChange,
            valueRange = valueRange,
            visibilityThreshold = (valueRange.endInclusive - valueRange.start) * 0.005f,
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlaygroundSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: PrismalBackdrop,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        PrismalGlassToggle(
            selected = { checked },
            onSelect = onCheckedChange,
            backdrop = backdrop
        )
    }
}

@Composable
private fun <T> PlaygroundEnumChips(
    label: String,
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    labelFor: (T) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                PlaygroundGlassButton(
                    onClick = { onSelected(option) },
                    backdrop = backdrop,
                    params = params,
                    luminance = luminance,
                    modifier = Modifier.weight(1f),
                    surfaceColor = if (option == selected) {
                        Color.White.copy(alpha = 0.15f)
                    } else {
                        Color.Unspecified
                    }
                ) {
                    Text(
                        labelFor(option),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
