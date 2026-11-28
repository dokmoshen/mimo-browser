package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.search.DuckResult
import app.mimo.browser.search.DuckSearchState
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.components.staggeredReveal
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserDuckResultsScreen(
    state: DuckSearchState,
    tabCount: Int,
    onBack: () -> Unit,
    onTabs: () -> Unit,
    onEditQuery: (String) -> Unit,
    onOpenResult: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenFallback: () -> Unit,
) {
    MimoBackdrop {
        Column(Modifier.fillMaxSize()) {
            ResultsHeader(
                state = state,
                tabCount = tabCount,
                onBack = onBack,
                onTabs = onTabs,
                onEditQuery = { onEditQuery(state.query) },
                onRetry = onRetry,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (state) {
                    is DuckSearchState.Loading -> LoadingBody()
                    is DuckSearchState.Failed -> ErrorBody(
                        onRetry = onRetry,
                        onOpenFallback = onOpenFallback,
                    )
                    is DuckSearchState.Ready -> {
                        if (state.results.isEmpty()) {
                            EmptyBody(
                                query = state.query,
                                onEditQuery = { onEditQuery(state.query) },
                                onOpenFallback = onOpenFallback,
                            )
                        } else {
                            ResultsList(
                                results = state.results,
                                onOpenResult = onOpenResult,
                                onEditQuery = { onEditQuery(state.query) },
                                onOpenFallback = onOpenFallback,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    state: DuckSearchState,
    tabCount: Int,
    onBack: () -> Unit,
    onTabs: () -> Unit,
    onEditQuery: () -> Unit,
    onRetry: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    val loading = state is DuckSearchState.Loading
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    haptics.tap()
                    onBack()
                },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    MimoIcons.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Surface(
                onClick = {
                    haptics.tap()
                    onEditQuery()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        MimoIcons.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "duckduckgo.com",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            TabCountBadge(count = tabCount, onClick = onTabs)
            IconButton(
                onClick = {
                    if (!loading) {
                        haptics.tap()
                        onRetry()
                    }
                },
                enabled = !loading,
                modifier = Modifier.size(44.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        BrowserIcons.Refresh,
                        contentDescription = "Обновить выдачу",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    haptics.tap()
                    onEditQuery()
                }
                .padding(start = 20.dp, end = 12.dp, top = 1.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    state.query,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    summaryLabel(state),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        MimoIcons.Edit,
                        contentDescription = "Изменить запрос",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBody() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(66.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                MimoLogo(
                    Modifier.size(38.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    accent = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Ищем в DuckDuckGo…",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Только результаты, без страницы поисковика.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorBody(
    onRetry: () -> Unit,
    onOpenFallback: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Не удалось получить выдачу",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "DuckDuckGo не ответил. Можно повторить или открыть его страницу.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Surface(
            onClick = onRetry,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                "Повторить",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onOpenFallback,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            ),
        ) {
            Text(
                "Открыть DuckDuckGo",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun EmptyBody(
    query: String,
    onEditQuery: () -> Unit,
    onOpenFallback: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Ничего не найдено",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "По запросу «$query» DuckDuckGo ничего не вернул.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Surface(
            onClick = onEditQuery,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                "Изменить запрос",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onOpenFallback,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            ),
        ) {
            Text(
                "Открыть DuckDuckGo",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun ResultsList(
    results: List<DuckResult>,
    onOpenResult: (String) -> Unit,
    onEditQuery: () -> Unit,
    onOpenFallback: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(results, key = { _, result -> result.url }) { index, result ->
            DuckResultCard(
                result = result,
                index = index,
                onClick = { onOpenResult(result.url) },
            )
        }
        item(key = "footer") {
            ResultsFooter(
                count = results.size,
                onEditQuery = onEditQuery,
                onOpenFallback = onOpenFallback,
            )
        }
    }
}

@Composable
private fun DuckResultCard(
    result: DuckResult,
    index: Int,
    onClick: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    val interaction = remember { MutableInteractionSource() }
    val host = result.displayUrl.ifBlank { UrlKit.host(result.url) }
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, 0.975f)
            .staggeredReveal(
                index = index,
                baseDelayMs = 40,
                stepMs = 22,
                offsetY = 14.dp,
                maxStaggerIndex = 8,
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SiteBadge(url = result.url, size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        result.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        host,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            BrowserIcons.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
            if (result.snippet.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    result.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ResultsFooter(
    count: Int,
    onEditQuery: () -> Unit,
    onOpenFallback: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Это все результаты",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "DuckDuckGo вернул ${resultsLabel(count)}. Можно уточнить запрос или открыть сайт.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = {
                        haptics.tap()
                        onEditQuery()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        "Новый запрос",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                Surface(
                    onClick = {
                        haptics.tap()
                        onOpenFallback()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    ),
                ) {
                    Text(
                        "Открыть сайт",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

private fun summaryLabel(state: DuckSearchState): String = when (state) {
    is DuckSearchState.Loading -> "DuckDuckGo · ищем результаты…"
    is DuckSearchState.Failed -> "DuckDuckGo · не удалось загрузить выдачу"
    is DuckSearchState.Ready -> if (state.results.isEmpty()) {
        "DuckDuckGo · ничего не найдено"
    } else {
        "DuckDuckGo · ${resultsLabel(state.results.size)} · без рекламы"
    }
}

private fun resultsLabel(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "$count результат"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "$count результата"
    else -> "$count результатов"
}
