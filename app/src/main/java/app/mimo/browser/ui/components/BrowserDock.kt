package app.mimo.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.ui.theme.LocalMimoDark

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
    historyCount: Int,
    downloadCount: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    desktopMode: Boolean,
    hasPage: Boolean,
    backdrop: GlassBackdrop? = null,
    pageBlurred: Boolean = false,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onNewTab: () -> Unit,
    onTabs: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
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
    val scrim = remember { MutableInteractionSource() }
    val windowView = LocalView.current
    val blurEnabled = LocalInterfaceBlur.current
    var panelScreen by remember { mutableStateOf<Rect?>(null) }
    val screen = panelScreen
    val showBackdrop = expanded && blurEnabled && !pageBlurred && backdrop != null && screen != null
    val glassActive = expanded && blurEnabled && (showBackdrop || pageBlurred)
    val dark = LocalMimoDark.current
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val tintStart = surfaceColor.copy(alpha = if (dark) 0.30f else 0.20f)
    val tintEnd = surfaceColor.copy(alpha = if (dark) 0.44f else 0.32f)

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
                animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
                label = "browserPanelWidth",
            )
            val height by animateDpAsState(
                targetValue = panelHeight,
                animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow),
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

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 10.dp)
                    .width(width)
                    .height(height)
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
                    }
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.14f, 1f)
                        scaleX = 0.92f + 0.08f * origin
                        scaleY = 0.96f + 0.04f * origin
                    },
                shape = RoundedCornerShape(corner),
                color = if (glassActive) Color.Transparent else surfaceColor.copy(alpha = 1f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                ),
                shadowElevation = if (expanded) {
                    if (glassActive) 0.dp else 24.dp
                } else {
                    14.dp
                },
            ) {
                val glow = MaterialTheme.colorScheme.primary
                val panelShape = RoundedCornerShape(corner)
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(panelShape),
                ) {
                    if (showBackdrop && backdrop != null && screen != null) {
                        GlassBackdropLayer(
                            backdrop = backdrop,
                            panel = screen,
                            blurRadius = 30.dp,
                            tint = Brush.verticalGradient(listOf(tintStart, tintEnd)),
                            modifier = Modifier.matchParentSize(),
                        )
                    } else if (glassActive) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(Brush.verticalGradient(listOf(tintStart, tintEnd))),
                        )
                    } else {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(surfaceColor.copy(alpha = 1f)),
                        )
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
                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = {
                            (fadeIn(tween(200, delayMillis = 70)) + scaleIn(
                                initialScale = 0.97f,
                                animationSpec = tween(220),
                            )).togetherWith(fadeOut(tween(80)))
                        },
                        label = "browserPanelContent",
                    ) { current ->
                        when (current) {
                            BrowserPanelMode.DOCK -> DockContent(
                                canGoBack = canGoBack,
                                canGoForward = canGoForward,
                                tabCount = tabCount,
                                onMenuClick = onMenuClick,
                                onBack = onBack,
                                onForward = onForward,
                                onNewTab = onNewTab,
                                onTabs = onTabs,
                            )
                            BrowserPanelMode.MENU -> MenuContent(
                                bookmarkCount = bookmarkCount,
                                historyCount = historyCount,
                                downloadCount = downloadCount,
                                desktopMode = desktopMode,
                                hasPage = hasPage,
                                onDismiss = onDismiss,
                                onBookmarks = onBookmarks,
                                onHistory = onHistory,
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
            }
        }
    }
}

@Composable
private fun DockContent(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onNewTab: () -> Unit,
    onTabs: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
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
            icon = BrowserIcons.Tabs,
            contentDescription = "Вкладки",
            onClick = onTabs,
            badge = tabCount.takeIf { it > 0 },
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
    val alpha by animateFloatAsState(if (enabled) 1f else 0.32f, label = "dockEnabled")
    Box(contentAlignment = Alignment.Center) {
        IconButton(
            onClick = {
                if (!enabled) return@IconButton
                haptics.tap()
                onClick()
            },
            modifier = Modifier.size(46.dp),
            enabled = enabled,
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { this.alpha = alpha },
            )
        }
        if (badge != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 3.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    text = if (badge > 99) "99" else badge.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun NewTabButton(onClick: () -> Unit) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = {
            haptics.press()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .pressScale(interaction, 0.92f)
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
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun MenuContent(
    bookmarkCount: Int,
    historyCount: Int,
    downloadCount: Int,
    desktopMode: Boolean,
    hasPage: Boolean,
    backdrop: GlassBackdrop? = null,
    pageBlurred: Boolean = false,
    onDismiss: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
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
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
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
            icon = BrowserIcons.Bookmark,
            label = "Закладки",
            count = bookmarkCount,
            onClick = onBookmarks,
        )
        MenuRow(
            icon = MimoIcons.History,
            label = "История",
            count = historyCount,
            onClick = onHistory,
        )
        MenuRow(
            icon = MimoIcons.FileDownload,
            label = "Загрузки",
            count = downloadCount,
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
                onClick = onShare,
            )
            MenuRow(
                icon = MimoIcons.ContentCopy,
                label = "Скопировать ссылку",
                onClick = onCopyLink,
            )
        }
        MenuRow(
            icon = BrowserIcons.Desktop,
            label = "Версия для компьютера",
            checked = desktopMode,
            onClick = onToggleDesktop,
        )
        if (hasPage) {
            MenuRow(
                icon = BrowserIcons.ExternalLink,
                label = "Открыть в другом браузере",
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
            onClick = onSettings,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    count: Int? = null,
    checked: Boolean = false,
    onClick: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .pressScale(interaction, 0.98f)
            .fillMaxWidth()
            .height(44.dp),
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
                modifier = Modifier.size(20.dp),
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
