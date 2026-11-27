package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.theme.Unbounded
import app.mimo.browser.web.TabState
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserTabsScreen(
    tabs: List<TabState>,
    activeId: Long?,
    onSelect: (Long) -> Unit,
    onClose: (Long) -> Unit,
    onNewTab: () -> Unit,
    onCloseAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmCloseAll by remember { mutableStateOf(false) }
    MimoBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(
                        MimoIcons.ArrowBack,
                        contentDescription = "Закрыть",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Вкладки",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        pluralTabs(tabs.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onNewTab, modifier = Modifier.size(44.dp)) {
                    Icon(
                        MimoIcons.Add,
                        contentDescription = "Новая вкладка",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (tabs.size > 1) {
                    IconButton(onClick = { confirmCloseAll = true }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            MimoIcons.DeleteSweep,
                            contentDescription = "Закрыть все",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "new-tab") {
                    NewTabCard(onClick = onNewTab)
                }
                items(tabs, key = { it.id }) { tab ->
                    TabCard(
                        tab = tab,
                        active = tab.id == activeId,
                        onClick = { onSelect(tab.id) },
                        onClose = { onClose(tab.id) },
                    )
                }
            }
        }
    }

    if (confirmCloseAll) {
        AlertDialog(
            onDismissRequest = { confirmCloseAll = false },
            title = { Text("Закрыть все вкладки?") },
            text = { Text("Останется одна пустая вкладка. Историю это не затронет.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmCloseAll = false
                    onCloseAll()
                }) { Text("Закрыть") }
            },
            dismissButton = {
                TextButton(onClick = { confirmCloseAll = false }) { Text("Отмена") }
            },
        )
    }
}

private fun pluralTabs(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "$count вкладка"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "$count вкладки"
    else -> "$count вкладок"
}

@Composable
private fun TabCard(
    tab: TabState,
    active: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .pressScale(interaction, 0.97f),
        shape = RoundedCornerShape(26.dp),
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f)
        },
        border = BorderStroke(
            if (active) 1.8.dp else 1.dp,
            if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
        shadowElevation = if (active) 6.dp else 0.dp,
    ) {
        val orbitPrimary = MaterialTheme.colorScheme.primary
        val orbitSecondary = MaterialTheme.colorScheme.secondary
        Box(Modifier.fillMaxSize()) {
            // Decorative orbit in the corner.
            androidx.compose.foundation.Canvas(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(66.dp),
            ) {
                drawCircle(
                    color = orbitPrimary.copy(alpha = 0.10f),
                    radius = size.minDimension * 0.45f,
                )
                drawArc(
                    color = orbitSecondary.copy(alpha = 0.35f),
                    startAngle = 150f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(2.dp.toPx(), size.height * 0.25f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.92f, size.height * 0.48f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.6.dp.toPx()),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    TabBadge(tab)
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            haptics.tap()
                            onClose()
                        },
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            MimoIcons.Close,
                            contentDescription = "Закрыть вкладку",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (tab.isHome) "Новая вкладка" else tab.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (tab.isHome) "адресная строка ждёт" else UrlKit.host(tab.url),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tab.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 1.8.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "загрузка",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (active) {
                        Box(
                            Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "сейчас",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabBadge(tab: TabState) {
    if (tab.isHome) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                MimoLogo(
                    Modifier.size(26.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    accent = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    } else {
        SiteBadge(url = tab.url, favicon = tab.favicon, size = 40.dp)
    }
}

@Composable
private fun NewTabCard(onClick: () -> Unit) {
    val haptics = LocalMimoHaptics.current
    Surface(
        onClick = {
            haptics.press()
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(MimoIcons.Add, contentDescription = null, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Новая вкладка",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
