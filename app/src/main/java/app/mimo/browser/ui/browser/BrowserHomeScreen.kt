package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.data.BookmarkEntity
import app.mimo.browser.data.HistoryEntity
import app.mimo.browser.ui.LibraryTab
import app.mimo.browser.ui.components.BrowserArt
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.BrowserIllustration
import app.mimo.browser.ui.components.CircleActionButton
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.ui.components.SectionHeader
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.theme.Unbounded
import app.mimo.browser.ui.util.formatTime
import app.mimo.browser.ui.util.isToday
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserHomeScreen(
    bookmarks: List<BookmarkEntity>,
    recentHistory: List<HistoryEntity>,
    onOpenOmnibox: () -> Unit,
    onOpenBookmark: (BookmarkEntity) -> Unit,
    onOpenHistory: (HistoryEntity) -> Unit,
    onOpenLibrary: (LibraryTab) -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 10.dp,
            bottom = 128.dp,
        ),
    ) {
        item {
            Spacer(
                Modifier
                    .statusBarsPadding()
                    .height(6.dp),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MimoLogo(
                        Modifier
                            .size(30.dp)
                            .padding(1.dp),
                        color = MaterialTheme.colorScheme.primary,
                        accent = MaterialTheme.colorScheme.secondary,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "mimo",
                            fontFamily = Unbounded,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "browser",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                CircleActionButton(
                    icon = BrowserIcons.Bookmark,
                    contentDescription = "Закладки",
                    onClick = { onOpenLibrary(LibraryTab.BOOKMARKS) },
                    modifier = Modifier.size(42.dp),
                    size = 42,
                )
                Spacer(Modifier.width(8.dp))
                CircleActionButton(
                    icon = MimoIcons.Settings,
                    contentDescription = "Настройки",
                    onClick = onOpenSettings,
                    modifier = Modifier.size(42.dp),
                    size = 42,
                )
            }
        }
        item { Spacer(Modifier.height(30.dp)) }
        item {
            Text(
                "интернет без суеты",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.8.sp),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Куда пойдём?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Адрес, запрос или тихое чтение — всё в одном месте.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
        item { Spacer(Modifier.height(22.dp)) }
        item {
            HomeSearchBar(onClick = onOpenOmnibox)
        }
        item { Spacer(Modifier.height(30.dp)) }
        item {
            SectionHeader(
                title = "Быстрый доступ",
                icon = null,
                trailing = if (bookmarks.isNotEmpty()) {
                    {
                        Text(
                            "все",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickableText { onOpenLibrary(LibraryTab.BOOKMARKS) },
                        )
                    }
                } else {
                    null
                },
            )
        }
        item { Spacer(Modifier.height(3.dp)) }
        item {
            if (bookmarks.isEmpty()) {
                EmptyQuickAccessCard(onAdd = onOpenOmnibox)
            } else {
                QuickAccessGrid(
                    bookmarks = bookmarks.take(8),
                    onOpenBookmark = onOpenBookmark,
                    onAdd = onOpenOmnibox,
                )
            }
        }
        item { Spacer(Modifier.height(30.dp)) }
        item {
            SectionHeader(
                title = "Недавно",
                trailing = if (recentHistory.isNotEmpty()) {
                    {
                        Text(
                            "история",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickableText { onOpenLibrary(LibraryTab.HISTORY) },
                        )
                    }
                } else {
                    null
                },
            )
        }
        item { Spacer(Modifier.height(3.dp)) }
        if (recentHistory.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BrowserArt(
                            kind = BrowserIllustration.HISTORY,
                            modifier = Modifier
                                .width(168.dp)
                                .padding(top = 8.dp),
                        )
                        Text(
                            "Здесь будет история",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Откройте первую страницу — и Mimo запомнит, где вы остановились.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        } else {
            items(recentHistory.take(6)) { entry ->
                HistoryRow(
                    entry = entry,
                    onClick = { onOpenHistory(entry) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    MimoIcons.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "История и закладки хранятся только на этом устройстве.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                )
            }
        }
    }
}

private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
        indication = null,
        onClick = onClick,
    )

@Composable
private fun HomeSearchBar(onClick: () -> Unit) {
    val interaction = androidx.compose.runtime.remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .pressScale(interaction, 0.985f),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 9.dp, end = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        MimoIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Поиск или адрес",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "весь интернет — в одной строке",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        BrowserIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyQuickAccessCard(onAdd: () -> Unit) {
    Surface(
        onClick = onAdd,
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        BrowserIcons.Star,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Сохраняйте любимое",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Звёздочка в адресной строке добавит страницу сюда.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        BrowserIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessGrid(
    bookmarks: List<BookmarkEntity>,
    onOpenBookmark: (BookmarkEntity) -> Unit,
    onAdd: () -> Unit,
) {
    val cells = buildList {
        bookmarks.forEach { add(QuickCell.Bookmark(it)) }
        if (bookmarks.size < 8 && (bookmarks.size < 4 || bookmarks.size % 4 != 0)) {
            add(QuickCell.Add)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cells.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { cell ->
                    Box(Modifier.weight(1f)) {
                        when (cell) {
                            is QuickCell.Bookmark -> BookmarkTile(
                                bookmark = cell.bookmark,
                                onClick = { onOpenBookmark(cell.bookmark) },
                            )
                            QuickCell.Add -> AddTile(onClick = onAdd)
                        }
                    }
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private sealed interface QuickCell {
    data class Bookmark(val bookmark: BookmarkEntity) : QuickCell
    data object Add : QuickCell
}

@Composable
private fun BookmarkTile(bookmark: BookmarkEntity, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SiteBadge(
                url = bookmark.url,
                size = 36.dp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = bookmark.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                text = UrlKit.host(bookmark.url),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                MimoIcons.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "сайт",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun HistoryRow(entry: HistoryEntity, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SiteBadge(url = entry.url, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        append(UrlKit.host(entry.url))
                        if (isToday(entry.visitedAt)) {
                            append(" · ")
                            append(formatTime(entry.visitedAt))
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                BrowserIcons.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
