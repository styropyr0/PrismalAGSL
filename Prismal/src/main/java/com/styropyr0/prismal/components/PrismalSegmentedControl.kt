package com.styropyr0.prismal.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import com.styropyr0.prismal.interactive.LocalPrismalHapticFeedback
import com.styropyr0.prismal.internal.rememberPrismalHaptics
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.PrismalGlass
import com.styropyr0.prismal.PrismalGlassPipeline
import com.styropyr0.prismal.PrismalGlassEffectProvider
import com.styropyr0.prismal.depth.PrismalDepthInset
import com.styropyr0.prismal.depth.PrismalDepthShadow
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.prismalLens
import com.styropyr0.prismal.interactive.PrismalSpringMotion
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedCornerStyle
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalMergedSource
import com.styropyr0.prismal.sources.rememberPrismalWrappedSource
import com.styropyr0.prismal.specular.PrismalSpecular
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

private val SegmentedHorizontalInset = 2.dp
private val SegmentedDropletVerticalInset = 0.dp
private val SegmentedTrackVerticalInset = 6.dp
private const val SegmentedDropletPressedScale = 1.14f
private const val SegmentedDropletMaxAspect = 2.2f
private const val SegmentedRefractionBand = 0.58f
private const val SegmentedRefractionAmount = 1f
private val SegmentedDropletShape = PrismalCapsule(PrismalRoundedCornerStyle.Circular)
private const val SegmentedUnselectedAlpha = 0.75f
private const val SegmentedStretchReferenceSpeedDp = 1600f
private const val SegmentedStretchWidth = 0.3f
private const val SegmentedStretchHeight = 0.16f
private const val SegmentedStretchDampingRatio = 0.4f
private const val SegmentedStretchStiffness = 380f
private val SegmentedOverdragMax = 5.dp
private const val SegmentedOverdragRange = 0f
private const val SegmentedOverdragStretchWidth = 0.12f
private const val SegmentedOverdragStretchHeight = 0.06f
private val SegmentedOverdragReleaseSpring = spring<Float>(dampingRatio = 0.45f, stiffness = 500f)
private val DefaultSegmentedTextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

/**
 * Segmented picker with a clear liquid-glass droplet riding over the selected label.
 *
 * Labels sit on a translucent capsule track with a resting thumb behind the selected one. The
 * droplet magnifies and refracts the track, thumb and labels under it across its whole dome. Tap a label to move the droplet, or
 * drag it across labels; it snaps to the nearest one on release. The droplet stretches
 * with its speed and wobbles when it stops, and rubber-bands past the first and last
 * segments while dragged.
 *
 * Refraction needs API 33+. Below that the droplet keeps its magnification, motion and
 * shadow.
 *
 * @param labels Segment titles; the control renders nothing when empty.
 * @param selectedIndex Index of the selected segment.
 * @param onSelected Called with the new index after a tap or a drag ends on another segment.
 * @param backdrop Source sampled through the lens.
 * @param trackColor Fill of the capsule track. When unspecified, an iOS-style translucent gray.
 * @param thumbColor Fill of the resting thumb behind the selected label. It fades out while the
 *   droplet is pressed. When unspecified, white in light theme and a soft white in dark theme.
 * @param textColor Label color. Defaults to black / white by theme; labels away from the
 *   lens are dimmed.
 * @param height Overall control height. The thumb spans all of it while the track is shorter, so
 *   the thumb stands taller than the track like the tab bar droplet.
 *   The droplet is at most 1.6× as wide as it is tall, so it stays round on wide segments.
 * @param magnification Scale of the content seen through the droplet at rest.
 * @param dropletTrackScale Scale of the track and resting thumb seen through the droplet while
 *   pressed, in `[0.5, 1]`. Below `1f` the track shrinks around the droplet's center so the
 *   backdrop shows around it like padding, while the labels keep their size. Only applies where
 *   the refracting lens is available (API 33+); `1f` disables it.
 * @param dropletEffects Replaces the lens's built-in effect stack when non-null.
 * @param specular Edge highlight on the lens; pass `null` to disable.
 * @param hapticFeedback When true, performs a haptic tick when the selected segment changes.
 */
@Composable
fun PrismalSegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = DefaultSegmentedTextStyle,
    textColor: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    thumbColor: Color = Color.Unspecified,
    height: Dp = 48.dp,
    magnification: Float = 1.3f,
    dropletTrackScale: Float = 0.83f,
    chromaticAberration: Float = 0.8f,
    luminance: () -> Float = { 0.5f },
    dropletEffects: (PrismalGlassEffectProvider.(luminance: Float) -> Unit)? = null,
    specular: (() -> PrismalSpecular?)? = { PrismalSpecular.Default },
    hapticFeedback: Boolean = LocalPrismalHapticFeedback.current,
) {
    val segmentCount = labels.size
    if (segmentCount == 0) return

    val density = LocalDensity.current
    val isLightTheme = !isSystemInDarkTheme()
    val resolvedTextColor =
        if (textColor.isSpecified) textColor
        else if (isLightTheme) Color.Black else Color.White

    val resolvedTrackColor =
        if (trackColor.isSpecified) trackColor
        else if (isLightTheme) Color(0xFF787880).copy(alpha = 0.16f)
        else Color(0xFF787880).copy(alpha = 0.36f)
    val resolvedThumbColor =
        if (thumbColor.isSpecified) thumbColor
        else if (isLightTheme) Color.White.copy(alpha = 0.92f)
        else Color.White.copy(alpha = 0.2f)

    val parentGlassLayer = LocalPrismalParentGlassLayer.current
    val underlayBackdrop = parentGlassLayer ?: backdrop
    val trackLayer = rememberPrismalGlassLayer()
    val labelsLayer = rememberPrismalGlassLayer()
    trackLayer.readSamplingState()
    labelsLayer.readSamplingState()
    parentGlassLayer?.readSamplingState()

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.CenterStart
    ) {
        val insetPx = with(density) { SegmentedHorizontalInset.toPx() }
        val segmentWidthPx = (constraints.maxWidth - insetPx * 2f) / segmentCount
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val haptics = rememberPrismalHaptics(hapticFeedback)
        val onSelectedState = rememberUpdatedState<(Int) -> Unit> {
            haptics.tick()
            onSelected(it)
        }
        val currentSelectedIndex by rememberUpdatedState(selectedIndex)
        val overdragMaxPx = with(density) { SegmentedOverdragMax.toPx() }
        val overdrag = remember { Animatable(0f) }
        val dropletStretch = remember { mutableFloatStateOf(0f) }

        val lensMotion = remember(animationScope, segmentCount, segmentWidthPx, isLtr, overdragMaxPx) {
            val maxValue = (segmentCount - 1).toFloat()
            val direction = if (isLtr) 1f else -1f
            var rawValue = 0f
            PrismalSpringMotion(
                animationScope = animationScope,
                initialValue = selectedIndex.coerceIn(0, segmentCount - 1).toFloat(),
                valueRange = 0f..maxValue,
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = SegmentedDropletPressedScale,
                onDragStarted = { rawValue = targetValue },
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().coerceIn(0, segmentCount - 1)
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch { overdrag.animateTo(0f, SegmentedOverdragReleaseSpring) }
                    if (targetIndex != currentSelectedIndex) {
                        onSelectedState.value(targetIndex)
                    }
                },
                onDrag = { _, dragAmount ->
                    if (segmentWidthPx <= 0f) return@PrismalSpringMotion
                    rawValue = (rawValue + dragAmount.x / segmentWidthPx * direction)
                        .fastCoerceIn(-SegmentedOverdragRange, maxValue + SegmentedOverdragRange)
                    val clamped = rawValue.fastCoerceIn(0f, maxValue)
                    updateValue(clamped)
                    val excessPx = (rawValue - clamped) * segmentWidthPx * direction
                    val resisted = overdragMaxPx * (1f - exp(-abs(excessPx) / overdragMaxPx))
                    animationScope.launch {
                        overdrag.snapTo(if (excessPx < 0f) -resisted else resisted)
                    }
                }
            )
        }

        val lensAvailable = PrismalGlass.pipeline == PrismalGlassPipeline.Liquid
        val shrunkTrack = rememberPrismalWrappedSource(trackLayer) { drawTrack ->
            val progress = if (lensAvailable) lensMotion.pressProgress else 0f
            scale(lerp(magnification, dropletTrackScale.fastCoerceIn(0.5f, 1f), progress)) {
                drawTrack()
            }
        }
        val scaledLensBackdrop = rememberPrismalMergedSource(shrunkTrack, labelsLayer)
        val mergedLensBackdrop = rememberPrismalMergedSource(underlayBackdrop, shrunkTrack, labelsLayer)
        val lensBackdrop =
            if (PrismalGlass.pipeline == PrismalGlassPipeline.Legacy) scaledLensBackdrop else mergedLensBackdrop

        LaunchedEffect(lensMotion, selectedIndex) {
            val target = selectedIndex.coerceIn(0, segmentCount - 1)
            if (lensMotion.targetValue.fastRoundToInt() != target) {
                lensMotion.animateToValue(target.toFloat())
            }
        }

        LaunchedEffect(lensMotion) {
            val damping = 2f * SegmentedStretchDampingRatio * sqrt(SegmentedStretchStiffness)
            var lastValue = lensMotion.value
            var stretchVelocity = 0f
            while (true) {
                if (abs(dropletStretch.floatValue) < 0.001f && abs(stretchVelocity) < 0.01f) {
                    dropletStretch.floatValue = 0f
                    stretchVelocity = 0f
                    val start = lastValue
                    snapshotFlow { lensMotion.value }.first { it != start }
                }
                var lastFrameNanos = withFrameNanos { it }
                while (true) {
                    val frameNanos = withFrameNanos { it }
                    val seconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).fastCoerceIn(0f, 1f / 30f)
                    lastFrameNanos = frameNanos
                    if (seconds <= 0f) continue

                    val value = lensMotion.value
                    val speedDp = abs(value - lastValue) * segmentWidthPx / density.density / seconds
                    lastValue = value
                    val target = (speedDp / SegmentedStretchReferenceSpeedDp).fastCoerceIn(0f, 1f)

                    val stretch = dropletStretch.floatValue
                    stretchVelocity += (SegmentedStretchStiffness * (target - stretch) -
                        damping * stretchVelocity) * seconds
                    dropletStretch.floatValue = stretch + stretchVelocity * seconds

                    if (target == 0f && abs(dropletStretch.floatValue) < 0.001f &&
                        abs(stretchVelocity) < 0.01f
                    ) break
                }
            }
        }

        val dropletHeight = height - SegmentedDropletVerticalInset * 2
        val dropletHeightPx = with(density) { dropletHeight.toPx() }
        val dropletWidthPx = segmentWidthPx
            .coerceAtMost(dropletHeightPx * SegmentedDropletMaxAspect)
            .coerceAtLeast(dropletHeightPx.coerceAtMost(segmentWidthPx))
        val dropletCenteringPx = (segmentWidthPx - dropletWidthPx) / 2f

        Box(
            Modifier
                .prismalGlassLayer(trackLayer)
                .fillMaxSize()
                .drawBehind {
                    val trackInset = SegmentedTrackVerticalInset.toPx()
                    val trackHeight = size.height - trackInset * 2f
                    drawRoundRect(
                        color = resolvedTrackColor,
                        topLeft = Offset(0f, trackInset),
                        size = Size(size.width, trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2f)
                    )
                    val alpha = (1f - lensMotion.pressProgress).coerceIn(0f, 1f)
                    if (alpha <= 0f) return@drawBehind
                    val travel = lensMotion.value * segmentWidthPx + dropletCenteringPx + overdrag.value
                    val left =
                        if (isLtr) insetPx + travel
                        else size.width - insetPx - travel - dropletWidthPx
                    val thumbHeight = size.height - SegmentedDropletVerticalInset.toPx() * 2f
                    drawRoundRect(
                        color = resolvedThumbColor,
                        topLeft = Offset(left, SegmentedDropletVerticalInset.toPx()),
                        size = Size(dropletWidthPx, thumbHeight),
                        cornerRadius = CornerRadius(thumbHeight / 2f),
                        alpha = alpha
                    )
                }
        )

        Row(
            Modifier
                .prismalGlassLayer(labelsLayer)
                .fillMaxSize()
                .padding(horizontal = SegmentedHorizontalInset),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { index, label ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = index == selectedIndex,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = {
                                lensMotion.animateToValue(index.toFloat())
                                if (index != selectedIndex) onSelectedState.value(index)
                            }
                        )
                        .graphicsLayer {
                            val distance = abs(lensMotion.value - index).fastCoerceIn(0f, 1f)
                            alpha = lerp(1f, SegmentedUnselectedAlpha, distance)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = label,
                        style = textStyle.copy(textAlign = TextAlign.Center),
                        color = { resolvedTextColor },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        Box(
            Modifier
                .padding(horizontal = SegmentedHorizontalInset)
                .graphicsLayer {
                    translationX =
                        (lensMotion.value * segmentWidthPx + dropletCenteringPx) * if (isLtr) 1f else -1f
                    translationX += overdrag.value
                }
                .then(lensMotion.modifier)
                .drawPrismalGlass(
                    backdrop = lensBackdrop,
                    shape = { SegmentedDropletShape },
                    effects = {
                        val currentLuminance = luminance()
                        if (dropletEffects != null) {
                            dropletEffects(currentLuminance)
                        } else {
                            val radius = size.minDimension / 2f
                            val glass = lensMotion.pressProgress.fastCoerceIn(0f, 1f)
                            prismalLens(
                                refractionHeight = radius * SegmentedRefractionBand * glass,
                                refractionAmount = radius * SegmentedRefractionAmount * glass,
                                depthEffect = true,
                                chromaticAberration = chromaticAberration
                            )
                        }
                    },
                    specular = specular,
                    depthShadow = {
                        PrismalDepthShadow(alpha = lerp(0.6f, 1f, lensMotion.pressProgress))
                    },
                    depthInset = {
                        val progress = lensMotion.pressProgress
                        PrismalDepthInset(
                            radius = 8.dp * lerp(0.75f, 1f, progress),
                            alpha = lerp(0.6f, 1f, progress)
                        )
                    },
                    layerBlock = {
                        val stretch = dropletStretch.floatValue
                        val overdragFraction = abs(overdrag.value) / overdragMaxPx
                        scaleX = lensMotion.scaleX *
                            (1f + stretch * SegmentedStretchWidth) *
                            (1f + overdragFraction * SegmentedOverdragStretchWidth)
                        scaleY = lensMotion.scaleY *
                            (1f - stretch * SegmentedStretchHeight) *
                            (1f - overdragFraction * SegmentedOverdragStretchHeight)
                    }
                )
                .width(with(density) { dropletWidthPx.toDp() })
                .height(dropletHeight)
        )
    }
}
