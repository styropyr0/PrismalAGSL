package com.styropyr0.prismaltest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.sources.PrismalGlassLayer

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
            IosSectionFooter("Drag the glass panel - styling follows Settings.")
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
