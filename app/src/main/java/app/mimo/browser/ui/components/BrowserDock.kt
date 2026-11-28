package app.mimo.browser.ui.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.ui.theme.LocalMimoDark
import kotlinx.coroutines.launch

enum class BrowserPanelMode { DOCK, MENU }

/**
 * One floating surface shared by the compact browser dock and the menu panel.
 * The container grows out of the dock, keeping the same bottom anchor and
 * expressive spring, rather than stacking a separate sheet on top.
 */
@Composable
fun BrowserSurfaceHost(
    mode: BrowserPanelMode,
    tabCount: Int,
    bookmarkCount: Int,
    downloadCount: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    desktopMode: Boolean,
    hasPage: Boolean,
    backdrop: GlassBackdrop? = null,
    onDockBounds: (Rect?) -> Unit = {},
    pageBlurred: Boolean = false,
    pageGlass: Boolean = false,
    solidBackground: Boolean = false,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onNewTab: () -> Unit,
    onTabs: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onBookmarks: () -> Unit,
    onDownloads: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onToggleDesktop: () -> Unit,
    onOpenExternal: () -> Unit,
    onSettings: () -> Unit,
) {
    BackHandler(enabled = mode == BrowserPanelMode.MENU) { onDismiss() }
    val expanded = mode == BrowserPanelMode.MENU
    val haptics = LocalMimoHaptics.current
    var collapsed by remember { mutableStateOf(false) }
    val hiddenOffset by animateDpAsState(
        targetValue = if (collapsed) 140.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "browserDockHidden",
    )
    val scrim = remember { MutableInteractionSource() }
    val windowView = LocalView.current
    val blurEnabled = LocalInterfaceBlur.current
    var panelScreen by remember { mutableStateOf<Rect?>(null) }
    val screen = panelScreen
    val panelProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "browserGlassProgress",
    )
    val menuGlass = blurEnabled && !solidBackground && panelProgress > 0.02f
    val dockGlass = !expanded && blurEnabled && pageGlass && !solidBackground
    val glassActive = menuGlass || dockGlass
    val showBackdrop = menuGlass && !pageBlurred && backdrop != null && screen != null
    val dark = LocalMimoDark.current
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val menuStartAlpha = when {
        pageBlurred -> if (dark) 0.52f else 0.44f
        else -> if (dark) 0.84f else 0.78f
    }
    val menuTint = Brush.verticalGradient(
        listOf(
            surfaceColor.copy(alpha = menuStartAlpha),
            surfaceColor.copy(alpha = menuStartAlpha + 0.12f),
        ),
    )
    val strongTint = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    val dockTint = Brush.verticalGradient(
        listOf(
            surfaceColor.copy(
                alpha = when {
                    strongTint -> if (dark) 0.92f else 0.95f
                    dark -> 0.36f
                    else -> 0.40f
                },
            ),
            surfaceColor.copy(
                alpha = when {
                    strongTint -> if (dark) 0.95f else 0.97f
                    dark -> 0.48f
                    else -> 0.50f
                },
            ),
        ),
    )

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(150)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.26f))
                    .clickable(interactionSource = scrim, indication = null) {
                        haptics.tap()
                        onDismiss()
                    },
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val panelWidth = if (expanded) {
                (maxWidth - 40.dp).coerceAtMost(360.dp)
            } else {
                302.dp
            }
            val panelHeight = if (expanded) {
                (maxHeight - 150.dp).coerceAtMost(if (hasPage) 440.dp else 320.dp)
            } else {
                68.dp
            }
            val width by animateDpAsState(
                targetValue = panelWidth,
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                label = "browserPanelWidth",
            )
            val height by animateDpAsState(
                targetValue = panelHeight,
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                label = "browserPanelHeight",
            )
            val corner by animateDpAsState(
                targetValue = if (expanded) 30.dp else 34.dp,
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                label = "browserPanelCorner",
            )
            val origin by animateFloatAsState(
                targetValue = if (expanded) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
                label = "browserPanelOrigin",
            )
            val dockAlpha by animateFloatAsState(
                targetValue = if (expanded) 0f else 1f,
                animationSpec = if (expanded) {
                    tween(120, delayMillis = 220, easing = FastOutSlowInEasing)
                } else {
                    tween(0)
                },
                label = "browserDockAlpha",
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 10.dp)
                    .width(width)
                    .height(height)
                    .offset(y = hiddenOffset)
                    .onGloballyPositioned { coordinates ->
                        val location = IntArray(2)
                        windowView.rootView.getLocationOnScreen(location)
                        val position = coordinates.positionInRoot()
                        panelScreen = Rect(
                            left = location[0] + position.x,
                            top = location[1] + position.y,
                            right = location[0] + position.x + coordinates.size.width,
                            bottom = location[1] + position.y + coordinates.size.height,
                        )
                        onDockBounds(
                            if (expanded) {
                                null
                            } else {
                                Rect(
                                    left = position.x,
                                    top = position.y,
                                    right = position.x + coordinates.size.width,
                                    bottom = position.y + coordinates.size.height,
                                )
                            },
                        )
                    },
                shape = RoundedCornerShape(corner),
                color = when {
                    menuGlass || dockGlass -> Color.Transparent
                    else -> surfaceColor.copy(alpha = 1f)
                },
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                ),
                shadowElevation = when {
                    glassActive -> 0.dp
                    expanded -> 24.dp
                    else -> 14.dp
                },
            ) {
                val glow = MaterialTheme.colorScheme.primary
                val panelShape = RoundedCornerShape(corner)
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(panelShape),
                ) {
                    when {
                        showBackdrop && backdrop != null && screen != null -> {
                            GlassBackdropLayer(
                                backdrop = backdrop,
                                panel = screen,
                                blurRadius = 42.dp,
                                tint = menuTint,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                        menuGlass -> {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .background(menuTint),
                            )
                        }
                        dockGlass -> {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .background(dockTint),
                            )
                        }
                        else -> {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .background(surfaceColor.copy(alpha = 1f)),
                            )
                        }
                    }
                    Canvas(Modifier.fillMaxSize()) {
                        val radius = size.maxDimension * (0.10f + 0.65f * origin)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glow.copy(alpha = 0.10f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width / 2f, size.height),
                                radius = radius,
                            ),
                            radius = radius,
                            center = Offset(size.width / 2f, size.height),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (collapsed) 0f else dockAlpha),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        DockContent(
                            canGoBack = canGoBack,
                            canGoForward = canGoForward,
                            onMenuClick = onMenuClick,
                            onBack = onBack,
                            onForward = onForward,
                            onNewTab = onNewTab,
                            onHide = {
                                haptics.tap()
                                collapsed = true
                            },
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(60)),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        MenuContent(
                            tabCount = tabCount,
                            bookmarkCount = bookmarkCount,
                            downloadCount = downloadCount,
                            desktopMode = desktopMode,
                            hasPage = hasPage,
                            onDismiss = onDismiss,
                            onTabs = onTabs,
                            onNewPrivateTab = onNewPrivateTab,
                            onBookmarks = onBookmarks,
                            onDownloads = onDownloads,
                            onShare = onShare,
                            onCopyLink = onCopyLink,
                            onToggleDesktop = onToggleDesktop,
                            onOpenExternal = onOpenExternal,
                            onSettings = onSettings,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = collapsed,
                enter = fadeIn(tween(180, delayMillis = 110)),
                exit = fadeOut(tween(100)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 6.dp)
                        .width(302.dp)
                        .height(18.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    haptics.tap()
                                    collapsed = false
                                },
                            )
                        },
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        color = surfaceColor.copy(alpha = 0.97f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        ),
                        shadowElevation = 6.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        CircleShape,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockContent(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onNewTab: () -> Unit,
    onHide: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DockButton(
            icon = MimoIcons.Menu,
            contentDescription = "Меню",
            onClick = onMenuClick,
        )
        DockButton(
            icon = MimoIcons.ArrowBack,
            contentDescription = "Назад",
            onClick = onBack,
            enabled = canGoBack,
        )
        NewTabButton(onClick = onNewTab)
        DockButton(
            icon = BrowserIcons.ArrowForward,
            contentDescription = "Вперёд",
            onClick = onForward,
            enabled = canGoForward,
        )
        DockButton(
            icon = MimoIcons.ArrowDownward,
            contentDescription = "Скрыть панель",
            onClick = onHide,
        )
    }
}

@Composable
private fun DockButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    badge: Int? = null,
) {
    val haptics = LocalMimoHaptics.current
    val scope = rememberCoroutineScope()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val alpha by animateFloatAsState(if (enabled) 1f else 0.32f, label = "dockEnabled")
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.16f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "dockIconScale",
    )
    val wobble = remember { Animatable(0f) }
    Box(contentAlignment = Alignment.Center) {
        IconButton(
            onClick = {
                if (!enabled) {
                    haptics.reject()
                    scope.launch {
                        wobble.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 320
                                -7f at 55
                                6f at 145
                                -3f at 230
                            },
                        )
                    }
                } else {
                    haptics.tap()
                    onClick()
                }
            },
            interactionSource = interaction,
            modifier = Modifier.size(46.dp),
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        this.alpha = alpha
                        scaleX = scale
                        scaleY = scale
                        rotationZ = wobble.value
                    },
            )
        }
        AnimatedVisibility(
            visible = badge != null,
            enter = scaleIn(
                initialScale = 0.45f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(tween(120)),
            exit = scaleOut(targetScale = 0.45f, animationSpec = tween(100)) +
                fadeOut(tween(100)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-3).dp, y = 3.dp),
        ) {
            AnimatedContent(
                targetState = badge ?: 1,
                transitionSpec = {
                    (scaleIn(
                        initialScale = 0.55f,
                        animationSpec = spring(
                            dampingRatio = 0.38f,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    ) + fadeIn(tween(100))).togetherWith(
                        scaleOut(targetScale = 0.55f, animationSpec = tween(80)) +
                            fadeOut(tween(80)),
                    )
                },
                label = "tabBadgeCount",
            ) { value ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        text = if (value > 99) "99" else value.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewTabButton(onClick: () -> Unit) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val rotation by animateFloatAsState(
        targetValue = if (pressed) 90f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "newTabRotation",
    )
    Surface(
        onClick = {
            haptics.press()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .pressScale(interaction, 0.90f)
            .size(54.dp),
        shape = RoundedCornerShape(19.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                MimoIcons.Add,
                contentDescription = "Новая вкладка",
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = rotation },
            )
        }
    }
}

@Composable
private fun MenuContent(
    tabCount: Int,
    bookmarkCount: Int,
    downloadCount: Int,
    desktopMode: Boolean,
    hasPage: Boolean,
    backdrop: GlassBackdrop? = null,
    pageBlurred: Boolean = false,
    pageGlass: Boolean = false,
    solidBackground: Boolean = false,
    onDismiss: () -> Unit,
    onTabs: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onBookmarks: () -> Unit,
    onDownloads: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onToggleDesktop: () -> Unit,
    onOpenExternal: () -> Unit,
    onSettings: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(start = 10.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Меню",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    haptics.tap()
                    onDismiss()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    MimoIcons.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
        Spacer(Modifier.height(2.dp))
        MenuRow(
            icon = BrowserIcons.Tabs,
            label = "Вкладки",
            count = tabCount,
            revealIndex = 0,
            onClick = {
                onDismiss()
                onTabs()
            },
        )
        MenuRow(
            icon = MimoIcons.VisibilityOff,
            label = "Новая вкладка инкогнито",
            revealIndex = 1,
            onClick = {
                onDismiss()
                onNewPrivateTab()
            },
        )
        MenuRow(
            icon = BrowserIcons.Bookmark,
            label = "Закладки",
            count = bookmarkCount,
            revealIndex = 2,
            onClick = onBookmarks,
        )
        MenuRow(
            icon = MimoIcons.FileDownload,
            label = "Загрузки",
            count = downloadCount,
            revealIndex = 3,
            onClick = onDownloads,
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
        if (hasPage) {
            MenuRow(
                icon = MimoIcons.Share,
                label = "Поделиться",
                revealIndex = 4,
                onClick = onShare,
            )
            MenuRow(
                icon = MimoIcons.ContentCopy,
                label = "Скопировать ссылку",
                revealIndex = 5,
                onClick = onCopyLink,
            )
        }
        MenuRow(
            icon = BrowserIcons.Desktop,
            label = "Версия для компьютера",
            checked = desktopMode,
            revealIndex = 6,
            onClick = onToggleDesktop,
        )
        if (hasPage) {
            MenuRow(
                icon = BrowserIcons.ExternalLink,
                label = "Открыть в другом браузере",
                revealIndex = 7,
                onClick = onOpenExternal,
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
        MenuRow(
            icon = MimoIcons.Settings,
            label = "Настройки",
            revealIndex = 8,
            onClick = onSettings,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    revealIndex: Int,
    count: Int? = null,
    checked: Boolean = false,
    onClick: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val iconScale by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "menuIconScale",
    )
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .staggeredReveal(
                index = revealIndex,
                baseDelayMs = 30,
                stepMs = 28,
                offsetY = 12.dp,
            )
            .pressScale(interaction, 0.98f)
            .fillMaxWidth()
            .height(38.dp),
        shape = RoundedCornerShape(15.dp),
        color = if (checked) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentColor = if (checked) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                tint = if (checked) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            if (count != null && count > 0) {
                Surface(
                    shape = CircleShape,
                    color = if (checked) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.10f)
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
            if (checked) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    MimoIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
