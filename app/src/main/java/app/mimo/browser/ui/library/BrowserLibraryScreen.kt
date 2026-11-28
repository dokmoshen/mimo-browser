package app.mimo.browser.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.data.BookmarkEntity
import app.mimo.browser.data.DownloadEntity
import app.mimo.browser.data.DownloadStatus
import app.mimo.browser.data.HistoryEntity
import app.mimo.browser.ui.LibraryTab
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserEmptyState
import app.mimo.browser.ui.components.BrowserIllustration
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.util.dateGroupLabel
import app.mimo.browser.ui.util.formatBytes
import app.mimo.browser.ui.util.formatShortDate
import app.mimo.browser.ui.util.formatTime
import app.mimo.browser.web.UrlKit
import kotlin.math.abs

@Composable
fun BrowserLibraryScreen(
    tab: LibraryTab,
    query: String,
    bookmarks: List<BookmarkEntity>,
    history: List<HistoryEntity>,
    downloads: List<DownloadEntity>,
    onTabChange: (LibraryTab) -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onDeleteBookmark: (BookmarkEntity) -> Unit,
    onDeleteHistory: (HistoryEntity) -> Unit,
    onOpenDownload: (DownloadEntity) -> Unit,
    onShareDownload: (DownloadEntity) -> Unit,
    onRemoveDownload: (DownloadEntity) -> Unit,
    onClearBookmarks: () -> Unit,
    onClearHistory: () -> Unit,
    onClearDownloads: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    val q = query.trim()
    val filteredBookmarks = remember(bookmarks, q) {
        if (q.isEmpty()) bookmarks else bookmarks.filter {
            it.title.contains(q, true) || it.url.contains(q, true)
        }
    }
    val filteredHistory = remember(history, q) {
        if (q.isEmpty()) history else history.filter {
            it.title.contains(q, true) || it.url.contains(q, true)
        }
    }
    val filteredDownloads = remember(downloads, q) {
        if (q.isEmpty()) downloads else downloads.filter {
            it.fileName.contains(q, true) || it.url.contains(q, true)
        }
    }
    val hasItems = when (tab) {
        LibraryTab.BOOKMARKS -> filteredBookmarks.isNotEmpty()
        LibraryTab.HISTORY -> filteredHistory.isNotEmpty()
        LibraryTab.DOWNLOADS -> filteredDownloads.isNotEmpty()
    }

    MimoBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        MimoIcons.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Библиотека",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "закладки и файлы",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (hasItems) {
                    IconButton(onClick = { confirmClear = true }, modifier = Modifier.size(46.dp)) {
                        Icon(
                            MimoIcons.DeleteSweep,
                            contentDescription = "Очистить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LibrarySegments(
                tab = tab,
                bookmarkCount = bookmarks.size,
                downloadCount = downloads.size,
                onTabChange = onTabChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            LibrarySearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(10.dp))
            when (tab) {
                LibraryTab.BOOKMARKS -> BookmarksContent(
                    bookmarks = filteredBookmarks,
                    query = q,
                    onOpen = { onOpenUrl(it.url) },
                    onDelete = onDeleteBookmark,
                )
                LibraryTab.HISTORY -> HistoryContent(
                    history = filteredHistory,
                    query = q,
                    onOpen = { onOpenUrl(it.url) },
                    onDelete = onDeleteHistory,
                )
                LibraryTab.DOWNLOADS -> DownloadsContent(
                    downloads = filteredDownloads,
                    query = q,
                    onOpen = onOpenDownload,
                    onShare = onShareDownload,
                    onDelete = onRemoveDownload,
                )
            }
        }
    }

    if (confirmClear) {
        val title = when (tab) {
            LibraryTab.BOOKMARKS -> "Очистить закладки?"
            LibraryTab.HISTORY -> "Очистить историю?"
            LibraryTab.DOWNLOADS -> "Очистить загрузки?"
        }
        val body = when (tab) {
            LibraryTab.BOOKMARKS -> "Все сохранённые страницы исчезнут. Это действие нельзя отменить."
            LibraryTab.HISTORY -> "Все посещения исчезнут. Это действие нельзя отменить."
            LibraryTab.DOWNLOADS -> "Список загрузок и скачанные файлы будут удалены."
        }
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    when (tab) {
                        LibraryTab.BOOKMARKS -> onClearBookmarks()
                        LibraryTab.HISTORY -> onClearHistory()
                        LibraryTab.DOWNLOADS -> onClearDownloads()
                    }
                }) { Text("Очистить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun LibrarySegments(
    tab: LibraryTab,
    bookmarkCount: Int,
    downloadCount: Int,
    onTabChange: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = when (tab) {
        LibraryTab.BOOKMARKS -> 0
        LibraryTab.HISTORY -> 0
        LibraryTab.DOWNLOADS -> 1
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .height(42.dp),
        ) {
            val segmentWidth = maxWidth / 2
            val pillOffset by animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "librarySegment",
            )
            Surface(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(segmentWidth)
                    .fillMaxHeight(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            ) {}
            Row(Modifier.fillMaxSize()) {
                fun selection(index: Int): Float {
                    val pillCenter = pillOffset.value + segmentWidth.value / 2f
                    val segmentCenter = segmentWidth.value * (index + 0.5f)
                    return (1f - abs(pillCenter - segmentCenter) / segmentWidth.value)
                        .coerceIn(0f, 1f)
                }
                Segment(
                    label = "Закладки",
                    count = bookmarkCount,
                    selected = tab == LibraryTab.BOOKMARKS,
                    selection = selection(0),
                    onClick = { onTabChange(LibraryTab.BOOKMARKS) },
                    modifier = Modifier.weight(1f),
                )
                Segment(
                    label = "Загрузки",
                    count = downloadCount,
                    selected = tab == LibraryTab.DOWNLOADS,
                    selection = selection(1),
                    onClick = { onTabChange(LibraryTab.DOWNLOADS) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun Segment(
    label: String,
    count: Int,
    selected: Boolean,
    selection: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(
        onClick = {
            haptics.toggle(selected)
            onClick()
        },
        interactionSource = interaction,
        modifier = modifier
            .fillMaxHeight()
            .pressScale(interaction, 0.96f),
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        val labelColor = lerp(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.onPrimary,
            selection,
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selection > 0.5f) FontWeight.Bold else FontWeight.Medium,
                color = labelColor,
                maxLines = 1,
            )
            if (count > 0) {
                Spacer(Modifier.width(5.dp))
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                MimoIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        "Поиск",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(38.dp)) {
                    Icon(
                        MimoIcons.Close,
                        contentDescription = "Очистить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarksContent(
    bookmarks: List<BookmarkEntity>,
    query: String,
    onOpen: (BookmarkEntity) -> Unit,
    onDelete: (BookmarkEntity) -> Unit,
) {
    if (bookmarks.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrowserEmptyState(
                title = if (query.isNotEmpty()) "Ничего не найдено" else "Закладок пока нет",
                body = if (query.isNotEmpty()) {
                    "Попробуйте другой запрос."
                } else {
                    "Нажмите на звёздочку в адресной строке, чтобы сохранить страницу."
                },
                kind = if (query.isNotEmpty()) BrowserIllustration.GLOBE else BrowserIllustration.BOOKMARKS,
            )
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(bookmarks, key = { it.id }) { bookmark ->
            LibraryPageRow(
                url = bookmark.url,
                title = bookmark.title,
                subtitle = UrlKit.host(bookmark.url),
                trailing = {
                    IconButton(
                        onClick = { onDelete(bookmark) },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            BrowserIcons.StarFilled,
                            contentDescription = "Удалить закладку",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                onClick = { onOpen(bookmark) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun HistoryContent(
    history: List<HistoryEntity>,
    query: String,
    onOpen: (HistoryEntity) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
) {
    if (history.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrowserEmptyState(
                title = if (query.isNotEmpty()) "Ничего не найдено" else "История пуста",
                body = if (query.isNotEmpty()) {
                    "Попробуйте другой запрос."
                } else {
                    "Страницы, которые вы открываете, появятся здесь."
                },
                kind = if (query.isNotEmpty()) BrowserIllustration.GLOBE else BrowserIllustration.HISTORY,
            )
        }
        return
    }
    val sections = remember(history) {
        history.groupBy { dateGroupLabel(it.visitedAt) }.toList()
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        sections.forEach { (label, entries) ->
            item(key = "section-$label") {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            items(entries, key = { "h-${it.id}" }) { entry ->
                LibraryPageRow(
                    url = entry.url,
                    title = entry.title,
                    subtitle = buildString {
                        append(UrlKit.host(entry.url))
                        append(" · ")
                        append(formatTime(entry.visitedAt))
                        if (!dateGroupLabel(entry.visitedAt).equals("Сегодня", true)) {
                            append(", ")
                            append(formatShortDate(entry.visitedAt))
                        }
                    },
                    trailing = {
                        IconButton(
                            onClick = { onDelete(entry) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                MimoIcons.Close,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    },
                    onClick = { onOpen(entry) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun DownloadsContent(
    downloads: List<DownloadEntity>,
    query: String,
    onOpen: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onDelete: (DownloadEntity) -> Unit,
) {
    if (downloads.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrowserEmptyState(
                title = if (query.isNotEmpty()) "Ничего не найдено" else "Загрузок нет",
                body = if (query.isNotEmpty()) {
                    "Попробуйте другой запрос."
                } else {
                    "Файлы, скачанные из браузера, будут ждать здесь."
                },
                kind = if (query.isNotEmpty()) BrowserIllustration.GLOBE else BrowserIllustration.DOWNLOAD,
            )
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(downloads, key = { "d-${it.id}" }) { download ->
            DownloadRow(
                download = download,
                onOpen = { onOpen(download) },
                onShare = { onShare(download) },
                onDelete = { onDelete(download) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun LibraryPageRow(
    url: String,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.975f),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 9.dp, bottom = 9.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SiteBadge(url = url, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailing()
        }
    }
}

@Composable
private fun DownloadRow(
    download: DownloadEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val complete = download.status == DownloadStatus.COMPLETE
    Surface(
        onClick = onOpen,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.975f),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 9.dp, bottom = 9.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(14.dp),
                color = when (download.status) {
                    DownloadStatus.COMPLETE -> MaterialTheme.colorScheme.primaryContainer
                    DownloadStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (complete) BrowserIcons.File else MimoIcons.FileDownload,
                        contentDescription = null,
                        tint = when (download.status) {
                            DownloadStatus.COMPLETE -> MaterialTheme.colorScheme.onPrimaryContainer
                            DownloadStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    download.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildString {
                        append(
                            when (download.status) {
                                DownloadStatus.COMPLETE -> "Скачано"
                                DownloadStatus.FAILED -> "Ошибка загрузки"
                                else -> "Скачивается…"
                            },
                        )
                        val size = formatBytes(download.totalBytes)
                        if (size.isNotEmpty()) {
                            append(" · ")
                            append(size)
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (download.status) {
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                )
            }
            if (complete) {
                IconButton(onClick = onShare, modifier = Modifier.size(38.dp)) {
                    Icon(
                        MimoIcons.Share,
                        contentDescription = "Поделиться",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                Icon(
                    MimoIcons.Delete,
                    contentDescription = "Удалить загрузку",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
