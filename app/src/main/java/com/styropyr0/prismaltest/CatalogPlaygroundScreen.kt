package com.styropyr0.prismaltest

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalGlassToggle
import com.styropyr0.prismal.components.PrismalGradientGlassPanel
import com.styropyr0.prismal.effects.rememberPrismalAdaptiveLuminance
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.PrismalGlassLayer
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer

private enum class PlaygroundTab {
    Home,
    Search,
    Profile,
    Settings
}

@Composable
fun CatalogPlaygroundScreen() {
    val backdropLayer = rememberPrismalGlassLayer()
    val glassParams = remember { GlassPlaygroundParams() }
    var selectedTab by remember { mutableIntStateOf(PlaygroundTab.Home.ordinal) }
    var toggleOn by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.45f) }
    var progress by remember { mutableFloatStateOf(0.65f) }
    var indeterminate by remember { mutableStateOf(false) }
    var backgroundImageUri by remember { mutableStateOf<Uri?>(null) }

    val isLightTheme = !isSystemInDarkTheme()
    val adaptiveLuminance = rememberPrismalAdaptiveLuminance(
        enabled = glassParams.adaptiveLuminance,
        source = backdropLayer,
        isLightTheme = isLightTheme
    )
    val luminance = { adaptiveLuminance.luminance }

    val pickBackgroundImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            backgroundImageUri = uri
        }
    }

    Box(Modifier.fillMaxSize()) {
        PlaygroundBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .prismalGlassLayer(backdropLayer),
            backgroundImageUri = backgroundImageUri
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                PrismalGlassBottomTabs(
                    selectedTabIndex = { selectedTab },
                    onTabSelected = { selectedTab = it },
                    backdrop = backdropLayer,
                    tabsCount = 4,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    IosTabItem(
                        selected = selectedTab == PlaygroundTab.Home.ordinal,
                        icon = Icons.Default.Home,
                        label = "Home",
                        onClick = { selectedTab = PlaygroundTab.Home.ordinal }
                    )
                    IosTabItem(
                        selected = selectedTab == PlaygroundTab.Search.ordinal,
                        icon = Icons.Default.Search,
                        label = "Search",
                        onClick = { selectedTab = PlaygroundTab.Search.ordinal }
                    )
                    IosTabItem(
                        selected = selectedTab == PlaygroundTab.Profile.ordinal,
                        icon = Icons.Default.Person,
                        label = "Profile",
                        onClick = { selectedTab = PlaygroundTab.Profile.ordinal }
                    )
                    IosTabItem(
                        selected = selectedTab == PlaygroundTab.Settings.ordinal,
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = { selectedTab = PlaygroundTab.Settings.ordinal }
                    )
                }
            },
            modifier = Modifier.padding(bottom = 12.dp)
        ) { innerPadding ->
            val contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)

            when (PlaygroundTab.entries[selectedTab]) {
                PlaygroundTab.Home -> CatalogTabContent(
                    backdrop = backdropLayer,
                    glassParams = glassParams,
                    luminance = luminance,
                    toggleOn = toggleOn,
                    onToggleChange = { toggleOn = it },
                    sliderValue = sliderValue,
                    onSliderValueChange = { sliderValue = it },
                    progress = progress,
                    indeterminate = indeterminate,
                    onIndeterminateChange = { indeterminate = it },
                    onAdvanceProgress = {
                        progress = ((progress + 0.15f) % 1.05f).coerceAtMost(1f)
                    },
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )

                PlaygroundTab.Search -> SearchTabContent(
                    backdrop = backdropLayer,
                    glassParams = glassParams,
                    luminance = luminance,
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )

                PlaygroundTab.Profile -> ProfileTabContent(
                    backdrop = backdropLayer,
                    glassParams = glassParams,
                    luminance = luminance,
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )

                PlaygroundTab.Settings -> GlassSettingsPlayground(
                    backdrop = backdropLayer,
                    backdropLayer = backdropLayer,
                    params = glassParams,
                    onPickWallpaper = {
                        pickBackgroundImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )
            }
        }
    }
}

@Composable
private fun RowScope.IosTabItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    PrismalGlassBottomTab(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) IosTheme.colors.systemBlue else IosTheme.colors.secondaryLabel
        )
        Text(
            text = label,
            style = IosTheme.tabLabel,
            color = if (selected) IosTheme.colors.systemBlue else IosTheme.colors.secondaryLabel,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CatalogTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    toggleOn: Boolean,
    onToggleChange: (Boolean) -> Unit,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    progress: Float,
    indeterminate: Boolean,
    onIndeterminateChange: (Boolean) -> Unit,
    onAdvanceProgress: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item { IosLargeTitle(title = "Browse", subtitle = "Prismal components") }

        item {
            IosSectionHeader("Buttons")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(IosLayout.groupInnerPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlaygroundGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        params = glassParams,
                        luminance = luminance,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Default", style = IosTheme.body, color = IosTheme.colors.label)
                    }
                    PlaygroundGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        params = glassParams,
                        luminance = luminance,
                        modifier = Modifier.weight(1f),
                        tint = Color(0xFF5856D6)
                    ) {
                        Text("Tinted", style = IosTheme.body, color = IosTheme.colors.label)
                    }
                }
            }
        }

        item {
            IosSectionHeader("Notifications")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosToggleRow(
                    title = "Allow Notifications",
                    subtitle = "Mirror iOS Settings toggle placement",
                    checked = toggleOn,
                    onCheckedChange = onToggleChange,
                    backdrop = backdrop
                )
            }
            IosSectionFooter("Glass toggles keep their built-in styling in this demo.")
        }

        item {
            IosSectionHeader("Intensity")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                Column(Modifier.padding(horizontal = IosLayout.groupInnerPadding, vertical = 14.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Level", style = IosTheme.body, color = IosTheme.colors.label)
                        Text(
                            "${"%.0f".format(sliderValue * 100)}%",
                            style = IosTheme.body,
                            color = IosTheme.colors.secondaryLabel
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    PrismalGlassSlider(
                        value = { sliderValue },
                        onValueChange = onSliderValueChange,
                        valueRange = 0f..1f,
                        visibilityThreshold = 0.01f,
                        backdrop = backdrop
                    )
                }
            }
        }

        item {
            IosSectionHeader("Download")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                Column(Modifier.padding(IosLayout.groupInnerPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlaygroundGlassProgressBar(
                        progress = { progress },
                        backdrop = backdrop,
                        params = glassParams,
                        luminance = luminance,
                        indeterminate = indeterminate
                    )
                    IosGroupDivider(showLeadingInset = false)
                    IosListRow(
                        title = if (indeterminate) "Use Determinate Progress" else "Use Indeterminate Progress",
                        titleColor = IosTheme.colors.systemBlue,
                        onClick = { onIndeterminateChange(!indeterminate) }
                    )
                    if (!indeterminate) {
                        IosGroupDivider(showLeadingInset = false)
                        IosListRow(
                            title = "Advance",
                            titleColor = IosTheme.colors.systemBlue,
                            onClick = onAdvanceProgress
                        )
                    }
                }
            }
        }

        item {
            IosSectionHeader("Showcase")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                Column(Modifier.padding(IosLayout.groupInnerPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrismalGradientGlassPanel(
                        backdrop = backdrop,
                        height = 140.dp,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        luminance = luminance,
                        blurRadiusDp = glassParams.blurRadiusDp.dp,
                        refractionHeightDp = glassParams.refractionHeightDp.dp,
                        refractionAmountDp = glassParams.refractionAmountDp.dp,
                        refractionBottomWeight = glassParams.gradientBottomWeight,
                        blurFadeEnd = glassParams.gradientBlurFadeEnd,
                        chromaticAberration = glassParams.chromaticAberration
                    )
                    IosGroupDivider(showLeadingInset = false)
                    PlaygroundGlassSurface(
                        backdrop = backdrop,
                        params = glassParams,
                        luminance = luminance,
                        shape = { PrismalRoundedRectangle(IosLayout.groupCorner) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        onClick = {},
                        content = {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = IosLayout.groupInnerPadding),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Glass Surface", style = IosTheme.headline, color = IosTheme.colors.label)
                                Text(
                                    "Tap for press ripple",
                                    style = IosTheme.footnote,
                                    color = IosTheme.colors.secondaryLabel
                                )
                            }
                        }
                    )
                }
            }
            IosSectionFooter("Component appearance follows values from the Settings tab.")
        }
    }
}

@Composable
private fun PlaygroundBackdrop(
    modifier: Modifier = Modifier,
    backgroundImageUri: Uri? = null
) {
    if (backgroundImageUri != null) {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(backgroundImageUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    DefaultPlaygroundBackdrop(modifier)
}

@Composable
private fun DefaultPlaygroundBackdrop(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val base = if (isDark) {
        listOf(Color(0xFF0B0B0F), Color(0xFF15151A), Color(0xFF1C1C22))
    } else {
        listOf(Color(0xFFDCE8FF), Color(0xFFE8DDF8), Color(0xFFF5E6EF))
    }

    Box(
        modifier.background(
            Brush.linearGradient(
                colors = base,
                start = Offset(0f, 0f),
                end = Offset(900f, 1600f)
            )
        )
    ) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = 80.dp)
                .size(280.dp)
                .clip(CircleShape)
                .background(Color(0xFF64D2FF).copy(alpha = if (isDark) 0.22f else 0.45f))
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 180.dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFFBF5AF2).copy(alpha = if (isDark) 0.18f else 0.38f))
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .size(320.dp)
                .clip(CircleShape)
                .background(Color(0xFF5E5CE6).copy(alpha = if (isDark) 0.12f else 0.28f))
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 120.dp)
                .size(200.dp)
                .clip(PrismalCapsule())
                .background(Color(0xFF32D74B).copy(alpha = if (isDark) 0.14f else 0.32f))
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 40.dp)
                .size(260.dp)
                .clip(PrismalRoundedRectangle(80.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A84FF).copy(alpha = if (isDark) 0.25f else 0.42f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
