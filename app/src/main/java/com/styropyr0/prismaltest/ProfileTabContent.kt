package com.styropyr0.prismaltest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.components.PrismalGlassButton
import com.styropyr0.prismal.components.PrismalSegmentedControl
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.PrismalGlassLayer

@Composable
fun ProfileTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var reduceMotion by remember { mutableStateOf(false) }
    var layoutIndex by remember { mutableIntStateOf(0) }

    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item { IosLargeTitle(title = "Profile") }

        // Identity card
        item {
            PlaygroundGlassSurface(
                backdrop = backdrop,
                params = glassParams,
                luminance = luminance,
                shape = { PrismalRoundedRectangle(glassParams.cornerRadiusDp.dp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IosLayout.screenHorizontal),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = IosLayout.groupInnerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlaygroundGlassSurface(
                        backdrop = backdrop,
                        params = glassParams,
                        luminance = luminance,
                        shape = { CircleShape },
                        modifier = Modifier.size(88.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("SS", style = IosTheme.title2, color = IosTheme.colors.label)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Saurav Sajeev",
                        style = IosTheme.title2,
                        color = IosTheme.colors.label
                    )
                    Text(
                        text = "Prismal tester · Liquid glass enthusiast",
                        style = IosTheme.footnote,
                        color = IosTheme.colors.secondaryLabel,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    PrismalGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        luminance = luminance,
                        modifier = Modifier.height(40.dp),
                        specular = glassParams.specularProvider(),
                    ) {
                        Text("Edit Profile", style = IosTheme.body, color = IosTheme.colors.label)
                    }
                }
            }
        }

        // Stats
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IosLayout.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("12", "Presets", backdrop, glassParams, luminance, Modifier.weight(1f))
                StatCard("48", "Widgets", backdrop, glassParams, luminance, Modifier.weight(1f))
                StatCard("3", "Wallpapers", backdrop, glassParams, luminance, Modifier.weight(1f))
            }
        }

        // Account
        item {
            IosSectionHeader("Account")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(title = "Name, Email", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Saved Presets", value = "12", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Subscriptions", showChevron = true, onClick = {})
            }
        }

        // Preferences
        item {
            IosSectionHeader("Preferences")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosToggleRow(
                    title = "Notifications",
                    subtitle = "New glass effects and releases",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it },
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosToggleRow(
                    title = "Haptic Feedback",
                    checked = PlaygroundHapticsSettings.enabled,
                    onCheckedChange = { PlaygroundHapticsSettings.enabled = it },
                    backdrop = backdrop
                )
                IosGroupDivider()
                IosToggleRow(
                    title = "Reduce Motion",
                    checked = reduceMotion,
                    onCheckedChange = { reduceMotion = it },
                    backdrop = backdrop
                )
            }
            IosSectionFooter("Preferences are local to this demo.")
        }

        item {
            IosSectionHeader("Layout")
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IosLayout.screenHorizontal)
            ) {
                PrismalSegmentedControl(
                    labels = listOf("Compact", "Comfortable", "Spacious"),
                    selectedIndex = layoutIndex,
                    onSelected = { layoutIndex = it },
                    backdrop = backdrop,
                    luminance = luminance,
                    specular = glassParams.specularProvider(),
                )
            }
        }

        // About + sign out
        item {
            IosSectionHeader("About")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(title = "Help & Feedback", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Privacy", showChevron = true, onClick = {})
                IosGroupDivider()
                IosListRow(title = "Sign Out", titleColor = IosTheme.colors.systemRed, onClick = {})
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    backdrop: PrismalGlassLayer,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
) {
    PlaygroundGlassSurface(
        backdrop = backdrop,
        params = params,
        luminance = luminance,
        shape = { PrismalRoundedRectangle(params.cornerRadiusDp.dp) },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = IosTheme.title2, color = IosTheme.colors.label)
            Text(label, style = IosTheme.caption1, color = IosTheme.colors.secondaryLabel)
        }
    }
}
