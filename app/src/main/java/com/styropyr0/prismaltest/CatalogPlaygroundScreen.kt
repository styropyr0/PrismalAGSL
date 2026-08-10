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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.styropyr0.prismal.components.LocalPrismalBottomTabHighlightedIndex
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalHorizontalSelector
import com.styropyr0.prismal.effects.rememberPrismalAdaptiveLuminance
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.PrismalGlassLayer
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalMergedSource

private enum class PlaygroundTab {
    Home,
    Search,
    Profile,
    Settings
}

@Composable
fun CatalogPlaygroundScreen() {
    val backdropLayer = rememberPrismalGlassLayer()
    val screenLayer = rememberPrismalGlassLayer()
    val modalBackdrop = rememberPrismalMergedSource(backdropLayer, screenLayer)
    val glassParams = rememberGlassPlaygroundParams()
    var selectedTab by remember { mutableIntStateOf(PlaygroundTab.Home.ordinal) }
    var toggleOn by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.45f) }
    var progress by remember { mutableFloatStateOf(0.65f) }
    var indeterminate by remember { mutableStateOf(false) }
    val backgroundImageUriState = rememberBackgroundImageUri()
    var backgroundImageUri by backgroundImageUriState
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var showDestructiveAlert by remember { mutableStateOf(false) }
    var cameraModeIndex by remember { mutableIntStateOf(1) }

    val context = LocalContext.current

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
            backgroundImageUri = persistBackgroundImage(context, uri) ?: uri
        }
    }

    Box(Modifier.fillMaxSize()) {
        PlaygroundBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .prismalGlassLayer(backdropLayer),
            backgroundImageUri = backgroundImageUri
        )

        Box(Modifier
            .fillMaxSize()
            .prismalGlassLayer(screenLayer)) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    PrismalGlassBottomTabs(
                        selectedTabIndex = { selectedTab },
                        onTabSelected = { selectedTab = it },
                        backdrop = backdropLayer,
                        tabsCount = 4,
                        specular = glassParams.specularProvider(),
                        dropletContentTint = IosTheme.colors.systemBlue,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)
                    ) {
                        IosTabItem(
                            index = PlaygroundTab.Home.ordinal,
                            icon = R.drawable.ic_home.asIcon(),
                            label = "Home",
                            onClick = { selectedTab = PlaygroundTab.Home.ordinal }
                        )
                        IosTabItem(
                            index = PlaygroundTab.Search.ordinal,
                            icon = R.drawable.ic_search.asIcon(),
                            label = "Search",
                            onClick = { selectedTab = PlaygroundTab.Search.ordinal }
                        )
                        IosTabItem(
                            index = PlaygroundTab.Profile.ordinal,
                            icon = R.drawable.ic_user.asIcon(),
                            label = "Profile",
                            onClick = { selectedTab = PlaygroundTab.Profile.ordinal }
                        )
                        IosTabItem(
                            index = PlaygroundTab.Settings.ordinal,
                            icon = R.drawable.ic_play.asIcon(),
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
                        onShowBottomSheet = { showBottomSheet = true },
                        onShowAlert = { showAlert = true },
                        onShowDestructiveAlert = { showDestructiveAlert = true },
                        cameraModeIndex = cameraModeIndex,
                        onCameraModeChange = { cameraModeIndex = it },
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
                        screenBackdrop = modalBackdrop,
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

        IosGlassBottomSheet(
            visible = showBottomSheet,
            onDismissRequest = { showBottomSheet = false },
            screenBackdrop = modalBackdrop,
            title = "Share"
        ) {
            IosPlainGroup {
                IosListRow(
                    title = "Copy Link",
                    showChevron = true,
                    onClick = { showBottomSheet = false })
                IosGroupDivider()
                IosListRow(
                    title = "Save Image",
                    showChevron = true,
                    onClick = { showBottomSheet = false })
                IosGroupDivider()
                IosListRow(
                    title = "Add to Reading List",
                    showChevron = true,
                    onClick = { showBottomSheet = false })
            }
        }

        IosGlassAlertDialog(
            visible = showAlert,
            onDismissRequest = { showAlert = false },
            screenBackdrop = modalBackdrop,
            title = "Allow Notifications?",
            message = "Prismal can send you updates about new glass effects and component releases.",
            confirmText = "Allow",
            cancelText = "Don't Allow"
        )

        IosGlassAlertDialog(
            visible = showDestructiveAlert,
            onDismissRequest = { showDestructiveAlert = false },
            screenBackdrop = modalBackdrop,
            title = "Delete Preset?",
            message = "This removes your saved glass configuration from this device.",
            confirmText = "Delete",
            cancelText = "Cancel",
            destructive = true,
            onConfirm = { glassParams.reset() }
        )
    }
}

@Composable
private fun RowScope.IosTabItem(index: Int, icon: ImageVector, label: String, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val selectedColor = if (isDark) Color.White else Color.Black
    val unselectedColor = if (isDark) Color.White else Color.Black
    val highlightedIndex = LocalPrismalBottomTabHighlightedIndex.current()
    val selected = highlightedIndex == index

    PrismalGlassBottomTab(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) selectedColor else unselectedColor
        )
        Text(
            text = label,
            style = IosTheme.tabLabel,
            color = if (selected) selectedColor else unselectedColor,
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
    onShowBottomSheet: () -> Unit,
    onShowAlert: () -> Unit,
    onShowDestructiveAlert: () -> Unit,
    cameraModeIndex: Int,
    onCameraModeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item { IosLargeTitle(title = "Browse", subtitle = "Prismal components") }

        item {
            IosSectionHeader("Horizontal Selector")
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IosLayout.screenHorizontal, vertical = 10.dp)
            ) {
                val cameraModes = remember {
                    listOf(
                        "VIDEO",
                        "PHOTO",
                        "PORTRAIT",
                        "SLO-MO",
                        "TIME-LAPSE",
                        "HYPERLAPSE",
                        "PRO",
                    )
                }
                PrismalHorizontalSelector(
                    labels = cameraModes,
                    selectedIndex = cameraModeIndex,
                    onSelected = onCameraModeChange,
                    backdrop = backdrop,
                    textStyle = IosTheme.caption1,
                    textColor = IosTheme.colors.label,
                    adaptiveLuminance = glassParams.adaptiveLuminance,
                    luminance = luminance,
                    specular = glassParams.specularProvider(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IosSectionFooter("Swipe horizontally — the liquid glass droplet stays put while items lock in.")
        }

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
                        tint = Color(0x8FFFEF82)
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
                Column(
                    Modifier.padding(
                        horizontal = IosLayout.groupInnerPadding,
                        vertical = 14.dp
                    )
                ) {
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
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = IosLayout.groupInnerPadding,
                            vertical = IosLayout.rowVerticalPadding
                        )
                ) {
                    PlaygroundGlassProgressBar(
                        progress = { progress },
                        backdrop = backdrop,
                        params = glassParams,
                        luminance = luminance,
                        indeterminate = indeterminate
                    )
                }
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

        item {
            IosSectionHeader("Dialogs")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(
                    title = "Liquid Glass Bottom Sheet",
                    showChevron = true,
                    onClick = onShowBottomSheet
                )
                IosGroupDivider()
                IosListRow(
                    title = "iOS Alert",
                    showChevron = true,
                    onClick = onShowAlert
                )
                IosGroupDivider()
                IosListRow(
                    title = "Destructive Alert",
                    titleColor = IosTheme.colors.systemRed,
                    showChevron = true,
                    onClick = onShowDestructiveAlert
                )
            }
            IosSectionFooter("Modals use a fixed iOS-style glass preset for consistent visibility.")
        }

        item {
            IosSectionHeader("Showcase")
            PlaygroundGlassSurface(
                backdrop = backdrop,
                params = glassParams,
                luminance = luminance,
                shape = { PrismalRoundedRectangle(glassParams.cornerRadiusDp.dp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(horizontal = IosLayout.rowHorizontalPadding),
                onClick = {},
                content = {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = IosLayout.groupInnerPadding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Glass Surface",
                            style = IosTheme.headline,
                            color = IosTheme.colors.label
                        )
                        Text(
                            "Tap for press ripple",
                            style = IosTheme.footnote,
                            color = IosTheme.colors.secondaryLabel
                        )
                    }
                }
            )
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

@Composable
fun Int.asIcon(): ImageVector {
    return ImageVector.vectorResource(this)
}