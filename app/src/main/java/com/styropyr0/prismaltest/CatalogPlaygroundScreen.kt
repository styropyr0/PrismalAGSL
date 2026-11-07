package com.styropyr0.prismaltest

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.styropyr0.prismal.PrismalGlass
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
import com.styropyr0.prismal.components.PrismalGlassButton
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.PrismalGlassMenu
import com.styropyr0.prismal.components.PrismalGlassMenuDivider
import com.styropyr0.prismal.components.PrismalGlassMenuItem
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalGlassStepper
import com.styropyr0.prismal.components.PrismalHorizontalSelector
import com.styropyr0.prismal.components.PrismalRangeSlider
import com.styropyr0.prismal.components.PrismalRulerSelector
import com.styropyr0.prismal.components.PrismalSegmentedControl
import com.styropyr0.prismal.components.prismalMenuAnchor
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
    Play,
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
    val backgroundImage = rememberBackgroundImageState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var showDestructiveAlert by remember { mutableStateOf(false) }
    var cameraModeIndex by remember { mutableIntStateOf(1) }
    var rulerValueMm by remember { mutableIntStateOf(25) }

    val context = LocalContext.current

    val isLightTheme = !isSystemInDarkTheme()
    val adaptiveLuminance = rememberPrismalAdaptiveLuminance(
        enabled = glassParams.adaptiveLuminance,
        source = backdropLayer,
        isLightTheme = isLightTheme
    )
    val luminance = { adaptiveLuminance.luminance }
    val isHighPerformanceDevice = rememberIsHighPerformanceDevice()
    val haptics = rememberPlaygroundHaptics()

    val pickBackgroundImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            backgroundImage.update(context, uri)
        }
    }

    key(PrismalGlass.pipeline) {
        Box(Modifier.fillMaxSize()) {
            PlaygroundBackdrop(
                modifier = Modifier
                    .fillMaxSize()
                    .prismalGlassLayer(backdropLayer),
                backgroundImageUri = backgroundImage.uri,
                imageCacheKey = backgroundImage.generation,
            )

            Box(Modifier
                .fillMaxSize()
                .prismalGlassLayer(screenLayer)) {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        PrismalGlassBottomTabs(
                            selectedTabIndex = { selectedTab },
                            onTabSelected = {
                                if (it != selectedTab) haptics.selectionTick()
                                selectedTab = it
                            },
                            backdrop = backdropLayer,
                            tabsCount = 5,
                            specular = glassParams.specularProvider(),
                            pressGlow = if (isLightTheme) 0.7f else 0.25f,
                            renderWithLowOverhead = !isHighPerformanceDevice,
                            modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)
                        ) {
                            IosTabItem(
                                icon = R.drawable.ic_home.asIcon(),
                                label = "Home",
                                onClick = { selectedTab = PlaygroundTab.Home.ordinal }
                            )
                            IosTabItem(
                                icon = R.drawable.ic_search.asIcon(),
                                label = "Search",
                                onClick = { selectedTab = PlaygroundTab.Search.ordinal }
                            )
                            IosTabItem(
                                icon = R.drawable.ic_game.asIcon(),
                                label = "Play",
                                onClick = { selectedTab = PlaygroundTab.Play.ordinal }
                            )
                            IosTabItem(
                                icon = R.drawable.ic_user.asIcon(),
                                label = "Profile",
                                onClick = { selectedTab = PlaygroundTab.Profile.ordinal }
                            )
                            IosTabItem(
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
                            screenBackdrop = modalBackdrop,
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
                            rulerValueMm = rulerValueMm,
                            onRulerValueChange = { rulerValueMm = it },
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

                        PlaygroundTab.Play -> PlayTabContent(
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
}

private fun tabUnselectedColor(isLightTheme: Boolean): Color =
    if (isLightTheme) Color.Black else Color.White

@Composable
private fun RowScope.IosTabItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val color = tabUnselectedColor(!isSystemInDarkTheme())

    PrismalGlassBottomTab(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(27.dp),
            tint = color
        )
        Text(
            text = label,
            style = IosTheme.tabLabel,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CatalogTabContent(
    backdrop: PrismalGlassLayer,
    screenBackdrop: PrismalBackdrop,
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
    rulerValueMm: Int,
    onRulerValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var segmentIndex by remember { mutableIntStateOf(1) }
    var quantity by remember { mutableIntStateOf(2) }
    var priceRange by remember { mutableStateOf(200f..650f) }
    val sortOptions = remember { listOf("Relevance", "Newest", "Price: Low to High", "Price: High to Low") }
    var sortIndex by remember { mutableIntStateOf(0) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var sortAnchor by remember { mutableStateOf(Rect.Zero) }

    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item { IosLargeTitle(title = "Browse", subtitle = "Prismal components") }

        item {
            IosSectionHeader("Segmented Control")
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IosLayout.screenHorizontal, vertical = 10.dp)
            ) {
                PrismalSegmentedControl(
                    labels = listOf("Day", "Week", "Month", "Year"),
                    selectedIndex = segmentIndex,
                    onSelected = { segmentIndex = it },
                    backdrop = backdrop,
                    textStyle = IosTheme.segmentedLabel,
                    luminance = luminance,
                    specular = glassParams.specularProvider(),
                )
            }
            IosSectionFooter("Tap a segment or drag the droplet between segments")
        }

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
                    textStyle = IosTheme.horizontalSelectorLabel,
                    textColor = IosTheme.colors.label,
                    chromaticAberration = 0.8f,
                    luminance = luminance,
                    specular = glassParams.specularProvider(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IosSectionFooter("Swipe horizontally to switch between options")
        }

        item {
            IosSectionHeader("Ruler Selector")
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = IosLayout.screenHorizontal, vertical = 10.dp)
            ) {
                PrismalRulerSelector(
                    value = rulerValueMm,
                    onValueChange = onRulerValueChange,
                    valueRange = 0..100,
                    step = 1,
                    majorTickEvery = 5,
                    valueLabel = { "${it}mm" },
                    backdrop = backdrop,
                    textStyle = IosTheme.horizontalSelectorLabel,
                    textColor = IosTheme.colors.label,
                    tickColor = IosTheme.colors.label.copy(alpha = 0.55f),
                    chromaticAberration = 0.8f,
                    luminance = luminance,
                    specular = glassParams.specularProvider(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IosSectionFooter("Swipe the ticks — the droplet value snaps as you scroll")
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
                    PrismalGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        luminance = luminance,
                        modifier = Modifier.weight(1f),
                        tintAlpha = glassParams.tintAlpha,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        adaptiveTuning = glassParams.adaptiveStyle.toTuning(),
                        effects = glassParams.buttonEffects(),
                        specular = glassParams.specularProvider(),
                        depthShadow = glassParams.depthShadowProvider(),
                        depthInset = glassParams.depthInsetProvider(),
                        onDrawSurface = glassParams.surfaceTintDrawer(),
                    ) {
                        Text("Default", style = IosTheme.body, color = IosTheme.colors.label)
                    }
                    PrismalGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        luminance = luminance,
                        modifier = Modifier.weight(1f),
                        tint = Color(0xFFFEF082),
                        tintAlpha = glassParams.tintAlpha,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        adaptiveTuning = glassParams.adaptiveStyle.toTuning(),
                        effects = glassParams.buttonEffects(),
                        specular = glassParams.specularProvider(),
                        depthShadow = glassParams.depthShadowProvider(),
                        depthInset = glassParams.depthInsetProvider(),
                        onDrawSurface = glassParams.surfaceTintDrawer(),
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
            IosSectionHeader("Price Range")
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
                        Text("Budget", style = IosTheme.body, color = IosTheme.colors.label)
                        Text(
                            "$${priceRange.start.toInt()} – $${priceRange.endInclusive.toInt()}",
                            style = IosTheme.body,
                            color = IosTheme.colors.secondaryLabel
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    PrismalRangeSlider(
                        value = { priceRange },
                        onValueChange = { priceRange = it },
                        valueRange = 0f..1000f,
                        visibilityThreshold = 1f,
                        minDistance = 50f,
                        backdrop = backdrop
                    )
                }
            }
            IosSectionFooter("Drag either thumb, or tap the track to move the nearest one")
        }

        item {
            IosSectionHeader("Stepper")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(
                    title = "Quantity",
                    value = "$quantity",
                    trailing = {
                        PrismalGlassStepper(
                            value = quantity,
                            onValueChange = { quantity = it },
                            backdrop = backdrop,
                            valueRange = 0..20,
                            luminance = luminance,
                            specular = glassParams.specularProvider(),
                        )
                    }
                )
            }
            IosSectionFooter("Hold − or + to keep stepping")
        }

        item {
            IosSectionHeader("Menu")
            IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                IosListRow(
                    title = "Sort By",
                    value = sortOptions[sortIndex],
                    showChevron = true,
                    onClick = { sortMenuOpen = true },
                    modifier = Modifier.prismalMenuAnchor { sortAnchor = it }
                )
            }
            PrismalGlassMenu(
                expanded = sortMenuOpen,
                onDismissRequest = { sortMenuOpen = false },
                anchorBounds = sortAnchor,
                backdrop = screenBackdrop,
            ) {
                sortOptions.forEachIndexed { index, label ->
                    if (index > 0) PrismalGlassMenuDivider()
                    PrismalGlassMenuItem(
                        text = label,
                        textStyle = IosTheme.menuItem,
                        selected = index == sortIndex,
                        onClick = {
                            sortIndex = index
                            sortMenuOpen = false
                        }
                    )
                }
            }
            IosSectionFooter("The menu grows out of a droplet at the row's edge")
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
    backgroundImageUri: Uri? = null,
    imageCacheKey: Int = 0,
) {
    if (backgroundImageUri != null) {
        val context = LocalContext.current
        key(imageCacheKey) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundImageUri)
                    .memoryCacheKey("playground-wallpaper-$imageCacheKey")
                    .diskCacheKey("playground-wallpaper-$imageCacheKey")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        }
        return
    }

    DefaultPlaygroundBackdrop(modifier)
}

@Composable
private fun DefaultPlaygroundBackdrop(modifier: Modifier = Modifier) {
    val wallpaper = if (isSystemInDarkTheme()) R.drawable.default_wallpaper
    else R.drawable.default_wallpaper_light
    Image(
        painter = painterResource(wallpaper),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun Int.asIcon(): ImageVector {
    return ImageVector.vectorResource(this)
}