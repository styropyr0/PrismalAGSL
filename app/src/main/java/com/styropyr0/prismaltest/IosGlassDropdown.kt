package com.styropyr0.prismaltest

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.shapes.PrismalRoundedCornerStyle
import kotlin.math.min

private val DropdownMorphSpring = spring<Float>(
    dampingRatio = 0.72f,
    stiffness = 520f,
)

private val DropdownMenuMinWidth = 180.dp
private val DropdownMenuMaxWidth = 280.dp
private val DropdownDropletSize = 30.dp
private const val DropdownRowHeightDp = 44

/** Bias width/height toward a square while morph is low so the seed stays circular. */
private fun dropdownSquareBias(morph: Float): Float = (1f - morph).coerceIn(0f, 1f).let { it * it }

/** Opacity for the whole popup: invisible at the droplet, fully visible once expanded. */
private fun dropdownPopupAlpha(morph: Float, closing: Boolean): Float =
    if (closing) {
        // Fade out before the morph reaches droplet size — last 20% of close is invisible.
        ((morph - 0.2f) / 0.8f).coerceIn(0f, 1f)
    } else {
        // Fade in during the first part of the open morph.
        (morph / 0.35f).coerceIn(0f, 1f)
    }

@Composable
fun IosGlassDropdownRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    screenBackdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }

    Box(modifier.fillMaxWidth()) {
        IosListRow(
            title = title,
            showChevron = true,
            onClick = { expanded = true },
            modifier = Modifier.onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                anchorBounds = Rect(
                    left = position.x,
                    top = position.y,
                    right = position.x + size.width,
                    bottom = position.y + size.height,
                )
            },
        )

        IosGlassDropdownMenu(
            expanded = expanded,
            anchorBounds = anchorBounds,
            options = options,
            selectedIndex = selectedIndex,
            onSelected = { index ->
                onSelected(index)
                expanded = false
            },
            onDismiss = { expanded = false },
            screenBackdrop = screenBackdrop,
            params = params,
            luminance = luminance,
        )
    }
}

@Composable
private fun IosGlassDropdownMenu(
    expanded: Boolean,
    anchorBounds: Rect,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    screenBackdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
) {
    if (anchorBounds == Rect.Zero) return

    screenBackdrop.readSamplingState()
    val density = LocalDensity.current
    val edgePaddingPx = with(density) { IosLayout.screenHorizontal.roundToPx() }
    val menuGapPx = with(density) { 4.dp.roundToPx() }
    val dropletPx = with(density) { DropdownDropletSize.toPx() }
    val transition = updateTransition(expanded, label = "dropdownMenu")

    val morph by transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> DropdownMorphSpring
                true isTransitioningTo false -> DropdownMorphSpring
                else -> snap()
            }
        },
        label = "morph",
    ) { open -> if (open) 1f else 0f }

    val scrimAlpha by transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> DropdownMorphSpring
                true isTransitioningTo false -> DropdownMorphSpring
                else -> snap()
            }
        },
        label = "scrimAlpha",
    ) { open -> if (open) 0.12f else 0f }

    val closing = !transition.targetState

    if (!expanded && morph <= 0.2f) {
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()

            val menuWidthPx = with(density) {
                anchorBounds.width
                    .toInt()
                    .coerceIn(
                        DropdownMenuMinWidth.roundToPx(),
                        DropdownMenuMaxWidth.roundToPx(),
                    )
            }
            val menuHeightPx = with(density) { (options.size * DropdownRowHeightDp).dp.roundToPx() }
            val menuWidthDp = with(density) { menuWidthPx.toDp() }
            val menuHeightDp = with(density) { menuHeightPx.toDp() }

            var targetX = anchorBounds.right - menuWidthPx
            targetX = targetX
                .coerceAtLeast(edgePaddingPx.toFloat())
                .coerceAtMost(screenWidthPx - menuWidthPx - edgePaddingPx)

            var targetY = anchorBounds.bottom + menuGapPx
            if (targetY + menuHeightPx > screenHeightPx - edgePaddingPx) {
                targetY = anchorBounds.top - menuHeightPx - menuGapPx
            }
            targetY = targetY.coerceAtLeast(edgePaddingPx.toFloat())

            val dropletX = anchorBounds.right - dropletPx
            val dropletY = anchorBounds.top + (anchorBounds.height - dropletPx) / 2f

            val popupX = lerp(dropletX, targetX, morph)
            val popupY = lerp(dropletY, targetY, morph)

            val rawWidth = lerp(DropdownDropletSize.value, menuWidthDp.value, morph)
            val rawHeight = lerp(DropdownDropletSize.value, menuHeightDp.value, morph)
            val squareSide = maxOf(rawWidth, rawHeight)
            val squareBias = dropdownSquareBias(morph)
            val popupWidth = lerp(rawWidth, squareSide, squareBias).dp
            val popupHeight = lerp(rawHeight, squareSide, squareBias).dp
            val popupAlpha = dropdownPopupAlpha(morph, closing)

            val popupShape: () -> Shape = {
                val cornerDp = lerp(
                    DropdownDropletSize.value / 2f,
                    params.cornerRadiusDp,
                    morph,
                ).coerceAtMost(min(popupWidth.value, popupHeight.value) / 2f)
                PrismalRoundedRectangle(
                    cornerRadius = cornerDp.dp,
                    style = PrismalRoundedCornerStyle.Continuous,
                )
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha * popupAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )

            Box(
                Modifier
                    .offset { IntOffset(popupX.toInt(), popupY.toInt()) }
                    .width(popupWidth)
                    .height(popupHeight)
                    .graphicsLayer { alpha = popupAlpha }
            ) {
                PlaygroundGlassSurface(
                    backdrop = screenBackdrop,
                    params = params,
                    luminance = luminance,
                    shape = popupShape,
                    useModalMaterial = true,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(Modifier.fillMaxSize()) {
                        options.forEachIndexed { index, label ->
                            if (index > 0) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = IosTheme.colors.separator,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (popupAlpha > 0.5f) {
                                            Modifier.clickable { onSelected(index) }
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .heightIn(min = DropdownRowHeightDp.dp)
                                    .padding(horizontal = IosLayout.rowHorizontalPadding),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(
                                    text = label,
                                    style = IosTheme.body,
                                    color = if (index == selectedIndex) {
                                        IosTheme.colors.systemBlue
                                    } else {
                                        IosTheme.colors.label
                                    },
                                )
                                if (index == selectedIndex) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = IosTheme.colors.systemBlue,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
