package com.styropyr0.prismaltest

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle

private val BottomSheetShape: Shape = AbsoluteRoundedCornerShape(
    topLeft = 34.dp,
    topRight = 34.dp,
    bottomRight = 0.dp,
    bottomLeft = 0.dp
)

private val AlertCornerRadius = 35.dp
private const val ModalScrimDim = 0.42f

private val ModalSquishEnterSpring = spring<Float>(
    dampingRatio = 0.54f,
    stiffness = 420f,
)

@Composable
private fun PlaygroundModalScrim(
    alpha: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    )
}

@Composable
private fun ModalGlassSurface(
    backdrop: PrismalBackdrop,
    shape: () -> Shape,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val surfaceTint = modalSurfaceTint()

    Box(modifier = modifier.wrapContentSize()) {
        Box(
            Modifier
                .matchParentSize()
                .drawPrismalGlass(
                    backdrop = backdrop,
                    shape = shape,
                    effects = modalGlassEffects(density),
                    onDrawSurface = {
                        drawRect(surfaceTint)
                    }
                )
        )
        content()
    }
}

@Composable
private fun PlaygroundSquishPopupOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    val transition = updateTransition(visible, label = "modalSquish")

    val scrimAlpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) tween(220) else tween(180)
        },
        label = "scrimAlpha",
    ) { shown -> if (shown) ModalScrimDim else 0f }

    val squishScaleX by transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> ModalSquishEnterSpring
                true isTransitioningTo false -> tween(150)
                else -> snap()
            }
        },
        label = "scaleX",
    ) { shown -> if (shown) 1f else 1.1f }

    val squishScaleY by transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> ModalSquishEnterSpring
                true isTransitioningTo false -> tween(150)
                else -> snap()
            }
        },
        label = "scaleY",
    ) { shown -> if (shown) 1f else 0.72f }

    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(180) },
        label = "contentAlpha",
    ) { shown -> if (shown) 1f else 0f }

    if (!visible && scrimAlpha <= 0f && contentAlpha <= 0f && !transition.isRunning) {
        return
    }

    Box(modifier.fillMaxSize()) {
        PlaygroundModalScrim(
            alpha = scrimAlpha,
            onDismiss = onDismiss
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = contentAlignment,
        ) {
            Box(
                Modifier.graphicsLayer {
                    scaleX = squishScaleX
                    scaleY = squishScaleY
                    alpha = contentAlpha
                }
            ) {
                content()
            }
        }
    }
}

@Composable
fun IosGlassBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    screenBackdrop: PrismalBackdrop,
    title: String,
    modifier: Modifier = Modifier,
    sheetContent: @Composable ColumnScope.() -> Unit = {}
) {
    val transition = updateTransition(visible, label = "bottomSheet")

    val scrimAlpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) tween(220) else tween(180)
        },
        label = "scrimAlpha",
    ) { shown -> if (shown) ModalScrimDim else 0f }

    val slideOffset by transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )

                true isTransitioningTo false -> spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMedium
                )

                else -> snap()
            }
        },
        label = "slideOffset",
    ) { shown -> if (shown) 0f else 1f }

    val sheetScaleY by transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> ModalSquishEnterSpring
                true isTransitioningTo false -> tween(160)
                else -> snap()
            }
        },
        label = "scaleY",
    ) { shown -> if (shown) 1f else 0.88f }

    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(180) },
        label = "contentAlpha",
    ) { shown -> if (shown) 1f else 0f }

    if (!visible && scrimAlpha <= 0f && contentAlpha <= 0f && !transition.isRunning) {
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        PlaygroundModalScrim(
            alpha = scrimAlpha,
            onDismiss = onDismissRequest
        )

        Box(
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val offsetY = slideOffset * size.height
                    translationY = offsetY
                    scaleX = 1f
                    scaleY = sheetScaleY
                    alpha = contentAlpha
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            ModalGlassSurface(
                backdrop = screenBackdrop,
                shape = { BottomSheetShape },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .width(36.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(IosTheme.colors.tertiaryLabel.copy(alpha = 0.45f))
                        )
                    }

                    Text(
                        text = title,
                        style = IosTheme.headline,
                        color = IosTheme.colors.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = IosLayout.groupInnerPadding),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))
                    sheetContent()
                }
            }
        }
    }
}

@Composable
fun IosGlassAlertDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    screenBackdrop: PrismalBackdrop,
    title: String,
    message: String,
    confirmText: String = "OK",
    cancelText: String? = null,
    destructive: Boolean = false,
    onConfirm: () -> Unit = onDismissRequest,
    onCancel: () -> Unit = onDismissRequest,
) {
    PlaygroundSquishPopupOverlay(
        visible = visible,
        onDismiss = onDismissRequest,
    ) {
        ModalGlassSurface(
            backdrop = screenBackdrop,
            shape = { PrismalRoundedRectangle(AlertCornerRadius) },
            modifier = Modifier
                .width(270.dp)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = {}
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = IosTheme.headline,
                        color = IosTheme.colors.label,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = message,
                        style = IosTheme.subheadline,
                        color = IosTheme.colors.secondaryLabel,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = IosTheme.colors.separator
                )

                if (cancelText != null) {
                    IosAlertButton(
                        text = cancelText,
                        color = IosTheme.colors.systemBlue,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onCancel()
                            onDismissRequest()
                        }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = IosTheme.colors.separator
                    )
                    IosAlertButton(
                        text = confirmText,
                        color = if (destructive) {
                            IosTheme.colors.systemRed
                        } else {
                            IosTheme.colors.systemBlue
                        },
                        bold = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onConfirm()
                            onDismissRequest()
                        }
                    )
                } else {
                    IosAlertButton(
                        text = confirmText,
                        color = IosTheme.colors.systemBlue,
                        bold = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onConfirm()
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IosAlertButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bold: Boolean = false
) {
    Box(
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (bold) IosTheme.body.copy(fontWeight = FontWeight.SemiBold)
            else IosTheme.body,
            color = color,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
