package com.styropyr0.prismal.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalMergedSource
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalWrappedSource
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.effects.prismalLens
import com.styropyr0.prismal.specular.PrismalSpecular
import com.styropyr0.prismal.interactive.PrismalSpringMotion
import com.styropyr0.prismal.interactive.PrismalPressRipple
import com.styropyr0.prismal.depth.PrismalDepthInset
import com.styropyr0.prismal.depth.PrismalDepthShadow
import com.styropyr0.prismal.shapes.PrismalCapsule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * iOS-style glass bottom tab bar with a sliding selection pill and press feedback.
 *
 * @param selectedTabIndex Index of the currently selected tab.
 * @param onTabSelected Called when the user selects a tab.
 * @param backdrop Source sampled through the tab bar glass.
 * @param tabsCount Number of tabs (must match [content] child count).
 * @param content Tab items, typically [PrismalGlassBottomTab] composables.
 * @param tintDropletContent When true, tints tab content sampled by the selection
 *   droplet. Set to false to keep the colors from [content].
 * @param dropletContentTint Color used when [tintDropletContent] is true. When `null`,
 *   defaults to iOS-style blue (`#0088FF` light / `#0091FF` dark). Pass any [Color]
 *   (e.g. [Color.Black]) to override.
 * @param specular The specular setting for the base pill
 * @param blurRadius Blur of the backdrop behind the bar.
 * @param refractionHeight Edge lens zone height of the bar.
 * @param refractionAmount Edge lens displacement of the bar.
 * @param dropletBlurRadius Blur of the background seen through the selection droplet
 *   (icons/labels stay sharp). `0.dp` disables it.
 * @param dropletChromaticAberration Dispersion strength of the droplet lens (`0f` = off;
 *   values above `1f` exaggerate the color split).
 * @param dropletRefractionHeight Edge lens zone height of the droplet while pressed.
 * @param dropletRefractionAmount Edge lens displacement of the droplet while pressed. Larger
 *   values also make the chromatic aberration more visible.
 * @param dropletContentScaleY Vertical scale of the tab row seen through the droplet while
 *   pressed, in `[0, 1]`. Lower values show a flatter, more shrunken bar; `1f` disables it.
 * @param pressGlow Intensity of the white press glow on the bar in `[0, 1]`.
 */
@Composable
fun PrismalGlassBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: PrismalBackdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    adaptiveLuminance: Boolean = false,
    luminance: () -> Float = { 0.5f },
    tintDropletContent: Boolean = true,
    dropletContentTint: Color? = null,
    specular: (() -> PrismalSpecular?)? = { PrismalSpecular.Default },
    blurRadius: Dp = 2.dp,
    refractionHeight: Dp = 24.dp,
    refractionAmount: Dp = 24.dp,
    dropletBlurRadius: Dp = 0.dp,
    dropletChromaticAberration: Float = 0.7f,
    dropletRefractionHeight: Dp = 20.dp,
    dropletRefractionAmount: Dp = 10.dp,
    pressGlow: Float = 0.35f,
    dropletContentScaleY: Float = 0.90f,
    content: @Composable RowScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val effectiveDropletTint = dropletContentTint ?: if (isLightTheme) Color(0xFF0088FF) else Color(0xFF0091FF)
    val containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(0.4f)
    else Color(0xFF121212).copy(0.4f)

    val tabsBackdrop = rememberPrismalGlassLayer()

    tabsBackdrop.readSamplingState()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8.dp.toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var currentIndex by remember(selectedTabIndex) {
            mutableIntStateOf(selectedTabIndex())
        }
        var dragHighlightIndex by remember { mutableStateOf<Int?>(null) }
        val highlightedTabIndex = dragHighlightIndex ?: selectedTabIndex()
        val dampedDragAnimation = remember(animationScope, tabsCount, tabWidth, isLtr) {
            PrismalSpringMotion(
                animationScope = animationScope,
                initialValue = selectedTabIndex().toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = DropletPressedScale,
                onDragStarted = {
                    dragHighlightIndex = value.fastRoundToInt().coerceIn(0, tabsCount - 1)
                },
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().coerceIn(0, tabsCount - 1)
                    dragHighlightIndex = targetIndex
                    currentIndex = targetIndex
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(1f, 300f, 0.5f)
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    val nextValue =
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f).fastCoerceIn(
                            0f,
                            (tabsCount - 1).toFloat()
                        )
                    updateValue(nextValue)
                    dragHighlightIndex = nextValue.fastRoundToInt()
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }
        LaunchedEffect(selectedTabIndex) {
            snapshotFlow { selectedTabIndex() }
                .collectLatest { index ->
                    currentIndex = index
                    dragHighlightIndex = null
                }
        }
        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { currentIndex }
                .drop(1)
                .collectLatest { index ->
                    dampedDragAnimation.animateToValue(index.toFloat())
                    onTabSelected(index)
                }
        }

        val shrunkTabsBackdrop = rememberPrismalWrappedSource(tabsBackdrop) { drawTabs ->
            val progress = dampedDragAnimation.pressProgress
            scale(1f, lerp(1f, dropletContentScaleY.fastCoerceIn(0f, 1f), progress)) {
                drawTabs()
            }
        }
        val dropletBackdrop = rememberPrismalMergedSource(backdrop, shrunkTabsBackdrop)

        val interactivePrismalSpecular = remember(animationScope, tabWidth, isLtr, pressGlow) {
            PrismalPressRipple(
                animationScope = animationScope,
                intensity = pressGlow,
                position = { size, _ ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        CompositionLocalProvider(
            LocalPrismalBottomTabScale provides {
                lerp(1f, 1.05f, dampedDragAnimation.pressProgress)
            },
            LocalPrismalBottomTabHighlightedIndex provides { highlightedTabIndex }
        ) {
            Row(
                Modifier
                    .graphicsLayer {
                        translationX = panelOffset
                    }
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { PrismalCapsule() },
                        specular = specular,
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = adaptiveLuminance,
                                luminance = luminance(),
                                blurRadiusPx = with(density) { blurRadius.toPx() },
                                refractionHeightPx = with(density) { refractionHeight.toPx() },
                                refractionAmountPx = with(density) { refractionAmount.toPx() }
                            )
                        },
                        layerBlock = {
                            val progress = dampedDragAnimation.pressProgress
                            val scale = lerp(1f, 1f + 16.dp.toPx() / size.width, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .then(interactivePrismalSpecular.modifier)
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )

            Row(
                Modifier
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .prismalGlassLayer(tabsBackdrop)
                    .graphicsLayer {
                        translationX = panelOffset
                    }
                    .drawPrismalGlass(
                        backdrop = backdrop,
                        shape = { PrismalCapsule() },
                        effects = {
                            applyPrismalGlassEffects(
                                density = density,
                                adaptiveLuminance = adaptiveLuminance,
                                luminance = luminance(),
                                blurRadiusPx = with(density) { dropletBlurRadius.toPx() }.let {
                                    if (adaptiveLuminance) it.coerceAtLeast(0.01f) else it
                                },
                                refractionHeightPx = 0f,
                                refractionAmountPx = 0f
                            )
                        },
                        specular = {
                            val progress = dampedDragAnimation.pressProgress
                            PrismalSpecular.Default.copy(alpha = progress)
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .then(interactivePrismalSpecular.modifier)
                    .height(56.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .then(
                        if (tintDropletContent) {
                            Modifier.drawWithContent {
                                val paint = Paint().apply {
                                    colorFilter = ColorFilter.tint(effectiveDropletTint)
                                }
                                drawContext.canvas.saveLayer(
                                    Rect(0f, 0f, size.width, size.height),
                                    paint
                                )
                                drawContent()
                                drawContext.canvas.restore()
                            }
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        Box(
            Modifier
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    translationX = (
                        if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                    ).fastRoundToInt().toFloat()
                }
                .then(interactivePrismalSpecular.gestureModifier)
                .then(dampedDragAnimation.modifier)
                .height(56.dp)
                .fillMaxWidth(1f / tabsCount)
                .layout { measurable, constraints ->
                    val w = constraints.maxWidth
                    val h = constraints.maxHeight
                    val velocity = dampedDragAnimation.velocity / 10f
                    val sx = dampedDragAnimation.scaleX / (1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f))
                    val sy = dampedDragAnimation.scaleY * (1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f))
                    val pw = (w * sx).fastRoundToInt()
                    val ph = (h * sy).fastRoundToInt()
                    val placeable = measurable.measure(Constraints.fixed(pw, ph))
                    layout(w, h) {
                        placeable.place((w - pw) / 2, (h - ph) / 2)
                    }
                }
                .drawPrismalGlass(
                    backdrop = dropletBackdrop,
                    shape = { PrismalCapsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        prismalLens(
                            refractionHeight = with(density) { dropletRefractionHeight.toPx() } * dampedDragAnimation.scaleY * progress,
                            refractionAmount = with(density) { dropletRefractionAmount.toPx() } * dampedDragAnimation.scaleY * progress,
                            depthEffect = true,
                            chromaticAberration = dropletChromaticAberration.coerceAtLeast(0f)
                        )
                    },
                    specular = {
                        val progress = dampedDragAnimation.pressProgress
                        PrismalSpecular.Default.copy(alpha = progress)
                    },
                    depthShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        PrismalDepthShadow(alpha = progress)
                    },
                    depthInset = {
                        val progress = dampedDragAnimation.pressProgress
                        PrismalDepthInset(
                            radius = 8.dp * dampedDragAnimation.scaleY * progress,
                            alpha = progress
                        )
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(
                            if (isLightTheme) Color.Black.copy(0.1f)
                            else Color.White.copy(0.1f),
                            alpha = 1f - progress
                        )
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    }
                )
        )
    }
}

private const val DropletPressedScale = 78f / 56f
