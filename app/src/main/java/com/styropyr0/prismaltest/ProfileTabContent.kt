package com.styropyr0.prismaltest

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.sources.PrismalGlassLayer
import kotlin.math.roundToInt

@Composable
fun ProfileTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val density = LocalDensity.current
    val panelWidth = 168.dp
    val panelHeight = 168.dp
    val panelWidthPx = with(density) { panelWidth.toPx() }
    val panelHeightPx = with(density) { panelHeight.toPx() }

    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlaygroundGlassSurface(
                    backdrop = backdrop,
                    params = glassParams,
                    luminance = luminance,
                    shape = { CircleShape },
                    modifier = Modifier.size(96.dp),
                    content = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SS",
                                style = IosTheme.title2,
                                color = IosTheme.colors.label
                            )
                        }
                    }
                )
                Text(
                    text = "Saurav Sajeev",
                    style = IosTheme.title2,
                    color = IosTheme.colors.label
                )
                Text(
                    text = "Apple ID, iCloud, Media & Purchases",
                    style = IosTheme.footnote,
                    color = IosTheme.colors.secondaryLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }
        }

        item {
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(title = "Name, Phone Numbers, Email", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Subscriptions", showChevron = true, onClick = {})
            }
        }

        item {
            IosSectionHeader("Widget Preview")
            IosSectionFooter("Drag the glass panel — styling follows Settings.")
        }

        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = IosLayout.screenHorizontal)
            ) {
                val maxWidthPx = constraints.maxWidth.toFloat()
                val maxHeightPx = constraints.maxHeight.toFloat()

                var offset by remember(maxWidthPx, maxHeightPx) {
                    mutableStateOf(
                        Offset(
                            (maxWidthPx - panelWidthPx) / 2f,
                            (maxHeightPx - panelHeightPx) / 2f
                        )
                    )
                }

                fun clampOffset(position: Offset): Offset =
                    Offset(
                        x = position.x.coerceIn(0f, (maxWidthPx - panelWidthPx).coerceAtLeast(0f)),
                        y = position.y.coerceIn(0f, (maxHeightPx - panelHeightPx).coerceAtLeast(0f))
                    )

                TunableGlassPanel(
                    backdrop = backdrop,
                    params = glassParams,
                    luminance = luminance,
                    modifier = Modifier
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .size(panelWidth, panelHeight)
                        .pointerInput(maxWidthPx, maxHeightPx) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offset = clampOffset(offset + dragAmount)
                            }
                        },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "Liquid Glass",
                                style = IosTheme.headline,
                                color = IosTheme.colors.label
                            )
                            Text(
                                text = "Drag anywhere",
                                style = IosTheme.caption1,
                                color = IosTheme.colors.secondaryLabel
                            )
                        }
                    }
                )
            }
        }

        item {
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(title = "Find My", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Family Sharing", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Sign Out", titleColor = IosTheme.colors.systemRed, onClick = {})
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
