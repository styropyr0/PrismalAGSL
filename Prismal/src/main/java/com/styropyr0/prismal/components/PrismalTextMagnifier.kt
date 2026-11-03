package com.styropyr0.prismal.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.prismalLens
import com.styropyr0.prismal.internal.PrismalPlatformMagnifierBridge
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalMergedSource
import com.styropyr0.prismal.sources.rememberPrismalWrappedSource
import com.styropyr0.prismal.specular.PrismalSpecular
import com.styropyr0.prismal.depth.PrismalDepthInset
import com.styropyr0.prismal.depth.PrismalDepthShadow
import kotlinx.coroutines.launch

/**
 * Hosts a text field and replaces its drag magnifier with an iOS-style glass loupe.
 *
 * While a cursor or selection handle of a text field inside [content] is dragged (including
 * long-press selection drags), a glass droplet pops up above the drag point showing a magnified
 * view of [content]. Android's own text magnifier (API 28+) is hidden for that field.
 *
 * Place the field's glass surface and the text field itself inside [content]:
 *
 * ```
 * PrismalTextMagnifierHost(backdrop = backdrop) {
 *     PrismalGlassSurface(backdrop = backdrop, shape = { PrismalCapsule() }) {
 *         BasicTextField(state = state, ...)
 *     }
 * }
 * ```
 *
 * Compose has no public magnifier API, so this hooks into its internals defensively: if a future
 * Compose version changes them, the system magnifier simply keeps working. Requires API 28+ (the
 * same as the system magnifier); below that nothing is shown. The loupe draws outside the host's
 * bounds, so a clipping ancestor (e.g. the edge of a scrolling list) can cut it off.
 *
 * @param backdrop What is behind the text field; seen around the magnified content.
 * @param enabled When false, the system magnifier is left untouched and no loupe is shown.
 * @param zoom Magnification of the loupe content.
 * @param size Size of the loupe.
 * @param lift How far above the dragged text line the loupe's center sits.
 * @param refractionHeight Edge lens zone height of the loupe.
 * @param refractionAmount Edge lens displacement of the loupe.
 * @param chromaticAberration Color split at the loupe's edge (`0f` = off).
 * @param surfaceColor Tint drawn over the magnified content.
 * @param specular Rim highlight of the loupe, or `null` for none.
 * @param depthShadow Drop shadow under the loupe, or `null` for none.
 * @param depthInset Inner edge shading of the loupe, or `null` for none.
 *
 * The defaults match the pressed [PrismalGlassBottomTabs] selection droplet, so both lenses look
 * the same.
 */
@Composable
fun PrismalTextMagnifierHost(
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    zoom: Float = 1.5f,
    size: DpSize = DpSize(120.dp, 60.dp),
    lift: Dp = 60.dp,
    refractionHeight: Dp = 30.dp,
    refractionAmount: Dp = 14.dp,
    chromaticAberration: Float = 0.7f,
    surfaceColor: Color = Color.Black.copy(alpha = 0.03f),
    specular: PrismalSpecular? = PrismalSpecular.Default,
    depthShadow: PrismalDepthShadow? = PrismalDepthShadow.Default,
    depthInset: PrismalDepthInset? = PrismalDepthInset(radius = 8.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val contentLayer = rememberPrismalGlassLayer()

    var hostCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var hasFocus by remember { mutableStateOf(false) }
    var focusPoint by remember { mutableStateOf(Offset.Unspecified) }
    var lastFocusPoint by remember { mutableStateOf(Offset.Zero) }
    val visibility = remember { Animatable(0f) }

    LaunchedEffect(enabled, hasFocus, view) {
        if (!enabled || !hasFocus) {
            focusPoint = Offset.Unspecified
            return@LaunchedEffect
        }
        var source: PrismalPlatformMagnifierBridge.Source? = null
        var framesUntilLookup = 0
        while (true) {
            withFrameNanos { }
            val coordinates = hostCoordinates?.takeIf { it.isAttached } ?: continue
            if (source == null) {
                if (framesUntilLookup-- > 0) continue
                framesUntilLookup = 15
                source = PrismalPlatformMagnifierBridge.find(
                    view = view,
                    boundsInRoot = coordinates.boundsInRoot(),
                    silenceSystemMagnifier = true
                ) ?: continue
            }
            val inRoot = source.position()
            focusPoint =
                if (inRoot.isSpecified) {
                    coordinates.localPositionOf(coordinates.findRootCoordinates(), inRoot)
                        .also { lastFocusPoint = it }
                } else {
                    Offset.Unspecified
                }
        }
    }

    val dragging = focusPoint.isSpecified
    LaunchedEffect(dragging) {
        launch {
            visibility.animateTo(
                if (dragging) 1f else 0f,
                spring(dampingRatio = if (dragging) 0.62f else 1f, stiffness = 520f)
            )
        }
    }

    val loupeWidthPx = with(density) { size.width.toPx() }
    val loupeHeightPx = with(density) { size.height.toPx() }
    val liftPx = with(density) { lift.toPx() }
    val screenMarginPx = with(density) { ScreenMargin.toPx() }
    val loupeTopLeft = run {
        val desired = Offset(
            lastFocusPoint.x - loupeWidthPx / 2f,
            lastFocusPoint.y - liftPx - loupeHeightPx / 2f
        )
        val coordinates = hostCoordinates?.takeIf { it.isAttached } ?: return@run desired
        val root = coordinates.findRootCoordinates()
        val hostInRoot = root.localPositionOf(coordinates, Offset.Zero)
        val rootWidth = root.size.width.toFloat()
        val rootHeight = root.size.height.toFloat()
        val minX = screenMarginPx - hostInRoot.x
        val maxX = rootWidth - screenMarginPx - loupeWidthPx - hostInRoot.x
        val minY = screenMarginPx - hostInRoot.y
        val maxY = rootHeight - screenMarginPx - loupeHeightPx - hostInRoot.y
        Offset(
            if (minX <= maxX) desired.x.coerceIn(minX, maxX) else desired.x,
            if (minY <= maxY) desired.y.coerceIn(minY, maxY) else desired.y
        )
    }

    val magnifiedContent = rememberPrismalWrappedSource(contentLayer) { drawContent ->
        val focusInLoupe = lastFocusPoint - loupeTopLeft
        withTransform({
            translate(loupeWidthPx / 2f, loupeHeightPx / 2f)
            scale(zoom, zoom, Offset.Zero)
            translate(-focusInLoupe.x, -focusInLoupe.y)
        }) {
            drawContent()
        }
    }
    val loupeBackdrop = rememberPrismalMergedSource(backdrop, magnifiedContent)

    Box(
        modifier
            .onGloballyPositioned { hostCoordinates = it }
            .onFocusChanged { hasFocus = it.hasFocus }
    ) {
        Box(Modifier.prismalGlassLayer(contentLayer), content = content)

        if (visibility.value > 0f) {
            Box(
                Modifier
                    .matchParentSize()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            Constraints.fixed(loupeWidthPx.fastRoundToInt(), loupeHeightPx.fastRoundToInt())
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(
                                IntOffset(loupeTopLeft.x.fastRoundToInt(), loupeTopLeft.y.fastRoundToInt())
                            )
                        }
                    }
                    .graphicsLayer {
                        val progress = visibility.value
                        alpha = progress.coerceIn(0f, 1f)
                        scaleX = 0.4f + 0.6f * progress
                        scaleY = 0.4f + 0.6f * progress
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                    .drawPrismalGlass(
                        backdrop = loupeBackdrop,
                        shape = { PrismalCapsule() },
                        effects = {
                            prismalLens(
                                refractionHeight = with(density) { refractionHeight.toPx() },
                                refractionAmount = with(density) { refractionAmount.toPx() },
                                depthEffect = true,
                                chromaticAberration = chromaticAberration.coerceAtLeast(0f)
                            )
                        },
                        specular = specular?.let { s -> { s } },
                        depthShadow = depthShadow?.let { d -> { d } },
                        depthInset = depthInset?.let { d -> { d } },
                        onDrawSurface = { drawRect(surfaceColor) }
                    )
            )
        }
    }
}

/** Minimum gap kept between the loupe and the window edges. */
private val ScreenMargin = 8.dp
