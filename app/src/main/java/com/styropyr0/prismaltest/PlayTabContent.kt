package com.styropyr0.prismaltest

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalGlassButton
import com.styropyr0.prismal.components.PrismalGlassProgressBar
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalGlassStepper
import com.styropyr0.prismal.components.PrismalGlassToggle
import com.styropyr0.prismal.components.PrismalHorizontalSelector
import com.styropyr0.prismal.components.PrismalRangeSlider
import com.styropyr0.prismal.components.PrismalRulerSelector
import com.styropyr0.prismal.components.PrismalSegmentedControl
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.effects.prismalLens
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.sources.PrismalGlassLayer
import kotlin.math.roundToInt

/** Which axes a widget may be dragged along on the stage. */
private enum class DragAxes {
    /** Free 2D drag. */
    Both,

    /**
     * Vertical only: these widgets already own a horizontal gesture
     * (scrub, swipe, drag-thumb), so horizontal movement belongs to them.
     */
    VerticalOnly
}

private enum class PlayWidget(val label: String, val axes: DragAxes) {
    GlassPanel("Glass Panel", DragAxes.Both),
    Droplet("Droplet", DragAxes.Both),
    LensDroplet("Heavy Lens", DragAxes.Both),
    PillDroplet("Pill Droplet", DragAxes.Both),
    Button("Button", DragAxes.Both),
    Stepper("Stepper", DragAxes.Both),
    Progress("Progress", DragAxes.Both),
    Segmented("Segmented", DragAxes.VerticalOnly),
    Selector("Selector", DragAxes.VerticalOnly),
    Ruler("Ruler", DragAxes.VerticalOnly),
    Slider("Slider", DragAxes.VerticalOnly),
    RangeSlider("Range Slider", DragAxes.VerticalOnly),
    Toggle("Toggle", DragAxes.VerticalOnly),
}

@Composable
fun PlayTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val widgets = remember { PlayWidget.entries }
    val labels = remember { widgets.map { it.label } }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selected = widgets[selectedIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        IosLargeTitle(title = "Playground", subtitle = "Pick a widget and drag it around")
        IosSectionHeader("Widget")
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = IosLayout.screenHorizontal, vertical = 6.dp)
        ) {
            PrismalHorizontalSelector(
                labels = labels,
                selectedIndex = selectedIndex,
                onSelected = {
                    selectedIndex = it
                },
                backdrop = backdrop,
                textStyle = IosTheme.horizontalSelectorLabel,
                textColor = IosTheme.colors.label,
                chromaticAberration = 0.8f,
                luminance = luminance,
                specular = glassParams.specularProvider(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        IosSectionFooter(
            if (selected.axes == DragAxes.Both) {
                "Drag the ${selected.label.lowercase()} anywhere on the stage."
            } else {
                "${selected.label} uses horizontal gestures itself, so it drags vertically only."
            }
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = IosLayout.screenHorizontal, vertical = 8.dp)
        ) {
            val stageWidth = maxWidth
            val stageWidthPx = constraints.maxWidth.toFloat()
            val stageHeightPx = constraints.maxHeight.toFloat()

            key(selected) {
                DraggableStageItem(
                    axes = selected.axes,
                    stageWidthPx = stageWidthPx,
                    stageHeightPx = stageHeightPx,
                ) {
                    PlayWidgetContent(
                        widget = selected,
                        stageWidth = stageWidth,
                        backdrop = backdrop,
                        glassParams = glassParams,
                        luminance = luminance,
                    )
                }
            }
        }
    }
}

/**
 * Centers [content] on the stage and lets the user drag it, clamped to the stage bounds.
 * With [DragAxes.VerticalOnly] horizontal drags are left to the widget itself.
 */
@Composable
private fun DraggableStageItem(
    axes: DragAxes,
    stageWidthPx: Float,
    stageHeightPx: Float,
    content: @Composable () -> Unit,
) {
    var delta by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var atEdge by remember { mutableStateOf(false) }
    val haptics = rememberPlaygroundHaptics()

    fun clamp(position: Offset): Offset {
        val maxX = ((stageWidthPx - size.width) / 2f).coerceAtLeast(0f)
        val maxY = ((stageHeightPx - size.height) / 2f).coerceAtLeast(0f)
        return Offset(
            x = if (axes == DragAxes.VerticalOnly) 0f else position.x.coerceIn(-maxX, maxX),
            y = position.y.coerceIn(-maxY, maxY),
        )
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .onSizeChanged { size = it }
                .offset { IntOffset(delta.x.roundToInt(), delta.y.roundToInt()) }
                .pointerInput(axes, stageWidthPx, stageHeightPx) {
                    fun moveBy(amount: Offset) {
                        val target = delta + amount
                        val clamped = clamp(target)
                        val hitEdge = if (axes == DragAxes.VerticalOnly) {
                            clamped.y != target.y
                        } else {
                            clamped != target
                        }
                        if (hitEdge && !atEdge) haptics.selectionTick()
                        atEdge = hitEdge
                        delta = clamped
                    }

                    when (axes) {
                        DragAxes.Both -> detectDragGestures(
                            onDragStart = { haptics.gestureStart() },
                            onDragEnd = { atEdge = false },
                            onDragCancel = { atEdge = false },
                        ) { change, dragAmount ->
                            change.consume()
                            moveBy(dragAmount)
                        }

                        DragAxes.VerticalOnly -> detectVerticalDragGestures(
                            onDragStart = { haptics.gestureStart() },
                            onDragEnd = { atEdge = false },
                            onDragCancel = { atEdge = false },
                        ) { change, dragAmount ->
                            change.consume()
                            moveBy(Offset(0f, dragAmount))
                        }
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
private fun PlayWidgetContent(
    widget: PlayWidget,
    stageWidth: Dp,
    backdrop: PrismalBackdrop,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
) {
    val wide = stageWidth * 0.92f

    when (widget) {
        PlayWidget.GlassPanel -> TunableGlassPanel(
            backdrop = backdrop,
            params = glassParams,
            luminance = luminance,
            modifier = Modifier.size(168.dp),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text("Liquid Glass", style = IosTheme.headline, color = IosTheme.colors.label)
                    Text(
                        "Drag anywhere",
                        style = IosTheme.caption1,
                        color = IosTheme.colors.secondaryLabel
                    )
                }
            }
        )

        PlayWidget.Droplet -> GlassDroplet(
            backdrop = backdrop,
            luminance = luminance,
            specular = glassParams,
            modifier = Modifier.size(150.dp),
            refractionHeight = 30.dp,
            refractionAmount = 20.dp,
            chromaticAberration = 0.3f,
        )

        PlayWidget.LensDroplet -> GlassDroplet(
            backdrop = backdrop,
            luminance = luminance,
            specular = glassParams,
            modifier = Modifier.size(190.dp),
            refractionHeight = 70.dp,
            refractionAmount = 64.dp,
            chromaticAberration = 0.9f,
        )

        PlayWidget.PillDroplet -> GlassDroplet(
            backdrop = backdrop,
            luminance = luminance,
            specular = glassParams,
            modifier = Modifier
                .width(wide.coerceAtMost(300.dp))
                .height(96.dp),
            refractionHeight = 40.dp,
            refractionAmount = 44.dp,
            chromaticAberration = 0.6f,
        )

        PlayWidget.Button -> PrismalGlassButton(
            onClick = {},
            backdrop = backdrop,
            luminance = luminance,
            modifier = Modifier
                .width(200.dp)
                .height(64.dp),
            tintAlpha = glassParams.tintAlpha,
            adaptiveLuminance = glassParams.adaptiveLuminance,
            adaptiveTuning = glassParams.adaptiveStyle.toTuning(),
            effects = glassParams.buttonEffects(),
            specular = glassParams.specularProvider(),
            depthShadow = glassParams.depthShadowProvider(),
            depthInset = glassParams.depthInsetProvider(),
            onDrawSurface = glassParams.surfaceTintDrawer(),
        ) {
            Text("Press me", style = IosTheme.body, color = IosTheme.colors.label)
        }

        PlayWidget.Stepper -> {
            var value by remember { mutableIntStateOf(2) }
            PrismalGlassStepper(
                value = value,
                onValueChange = {
                    value = it
                },
                backdrop = backdrop,
                valueRange = 0..20,
                luminance = luminance,
                specular = glassParams.specularProvider(),
            )
        }

        PlayWidget.Progress -> {
            var progress by remember { mutableFloatStateOf(0.65f) }
            Column(
                Modifier.width(wide),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlaygroundGlassProgressBar(
                    progress = { progress },
                    backdrop = backdrop,
                    params = glassParams,
                    luminance = luminance,
                )
                Text(
                    "${(progress * 100).roundToInt()}%",
                    style = IosTheme.footnote,
                    color = IosTheme.colors.secondaryLabel
                )
                PrismalGlassButton(
                    onClick = {
                        progress = ((progress + 0.15f) % 1.05f).coerceAtMost(1f)
                    },
                    backdrop = backdrop,
                    luminance = luminance,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(44.dp),
                    specular = glassParams.specularProvider(),
                ) {
                    Text("Advance", style = IosTheme.body, color = IosTheme.colors.label)
                }
            }
        }

        PlayWidget.Segmented -> {
            var index by remember { mutableIntStateOf(1) }
            PrismalSegmentedControl(
                labels = listOf("Day", "Week", "Month", "Year"),
                selectedIndex = index,
                onSelected = {
                    index = it
                },
                backdrop = backdrop,
                textStyle = IosTheme.segmentedLabel,
                luminance = luminance,
                specular = glassParams.specularProvider(),
                modifier = Modifier.width(wide),
            )
        }

        PlayWidget.Selector -> {
            var index by remember { mutableIntStateOf(1) }
            val modes = remember {
                listOf("VIDEO", "PHOTO", "PORTRAIT", "SLO-MO", "TIME-LAPSE", "PRO")
            }
            PrismalHorizontalSelector(
                labels = modes,
                selectedIndex = index,
                onSelected = {
                    index = it
                },
                backdrop = backdrop,
                textStyle = IosTheme.horizontalSelectorLabel,
                textColor = IosTheme.colors.label,
                chromaticAberration = 0.8f,
                luminance = luminance,
                specular = glassParams.specularProvider(),
                modifier = Modifier.width(wide),
            )
        }

        PlayWidget.Ruler -> {
            var value by remember { mutableIntStateOf(25) }
            PrismalRulerSelector(
                value = value,
                onValueChange = {
                    value = it
                },
                valueRange = 0..100,
                step = 1,
                majorTickEvery = 5,
                valueLabel = { "${it}mm" },
                backdrop = backdrop,
                textStyle = IosTheme.horizontalSelectorLabel,
                textColor = IosTheme.colors.label,
                tickColor = IosTheme.colors.label.copy(alpha = 0.55f),
                chromaticAberration = 0.8f,
                luminance = luminance,
                specular = glassParams.specularProvider(),
                modifier = Modifier.width(wide),
            )
        }

        PlayWidget.Slider -> {
            var value by remember { mutableFloatStateOf(0.45f) }
            Column(Modifier.width(wide)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Level", style = IosTheme.body, color = IosTheme.colors.label)
                    Text(
                        "${(value * 100).roundToInt()}%",
                        style = IosTheme.body,
                        color = IosTheme.colors.secondaryLabel
                    )
                }
                Box(Modifier.height(10.dp))
                PrismalGlassSlider(
                    value = { value },
                    onValueChange = {
                        value = it
                    },
                    valueRange = 0f..1f,
                    visibilityThreshold = 0.01f,
                    backdrop = backdrop,
                    luminance = luminance,
                )
            }
        }

        PlayWidget.RangeSlider -> {
            var range by remember { mutableStateOf(200f..650f) }
            Column(Modifier.width(wide)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Budget", style = IosTheme.body, color = IosTheme.colors.label)
                    Text(
                        "$${range.start.toInt()} – $${range.endInclusive.toInt()}",
                        style = IosTheme.body,
                        color = IosTheme.colors.secondaryLabel
                    )
                }
                Box(Modifier.height(10.dp))
                PrismalRangeSlider(
                    value = { range },
                    onValueChange = {
                        range = it
                    },
                    valueRange = 0f..1000f,
                    visibilityThreshold = 1f,
                    minDistance = 50f,
                    backdrop = backdrop,
                    luminance = luminance,
                )
            }
        }

        PlayWidget.Toggle -> {
            var on by remember { mutableStateOf(true) }
            PrismalGlassToggle(
                selected = { on },
                onSelect = {
                    on = it
                },
                backdrop = backdrop,
                luminance = luminance,
            )
        }
    }
}

/**
 * Standalone refracting glass droplet. Square sizes give a circle, wide sizes a pill.
 * The refraction values control how strongly the backdrop bends at the rim.
 */
@Composable
private fun GlassDroplet(
    backdrop: PrismalBackdrop,
    luminance: () -> Float,
    specular: GlassPlaygroundParams,
    refractionHeight: Dp,
    refractionAmount: Dp,
    chromaticAberration: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Box(
        modifier.drawPrismalGlass(
            backdrop = backdrop,
            shape = { PrismalCapsule() },
            effects = {
                applyPrismalGlassEffects(
                    density = density,
                    adaptiveLuminance = false,
                    luminance = luminance(),
                    blurRadiusPx = 0f,
                    refractionHeightPx = 0f,
                    refractionAmountPx = 0f,
                    useVibrancy = true,
                )
                prismalLens(
                    refractionHeight = with(density) { refractionHeight.toPx() },
                    refractionAmount = with(density) { refractionAmount.toPx() },
                    chromaticAberration = chromaticAberration,
                )
            },
            specular = specular.specularProvider(),
            depthShadow = specular.depthShadowProvider(),
        )
    )
}
