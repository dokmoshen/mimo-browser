package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.search.DuckResult
import app.mimo.browser.search.DuckSearchState
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.components.staggeredReveal
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserDuckResultsScreen(
    state: DuckSearchState,
    onBack: () -> Unit,
    onEditQuery: (String) -> Unit,
    onOpenResult: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenFallback: () -> Unit,
) {
    MimoBackdrop {
        Column(Modifier.fillMaxSize()) {
            ResultsTopBar(
                query = state.query,
                loading = state is DuckSearchState.Loading,
                onBack = onBack,
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
private fun ResultsTopBar(
    query: String,
    loading: Boolean,
    onBack: () -> Unit,
    onEditQuery: () -> Unit,
    onRetry: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                .height(52.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            ),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    MimoIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    query,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(
            onClick = {
                if (!loading) {
                    haptics.tap()
                    onRetry()
                }
            },
            enabled = !loading,
            modifier = Modifier.size(48.dp),
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
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            strokeWidth = 2.4.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Ищем в DuckDuckGo…",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Показываем только результаты, без страницы поисковика.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
    onOpenFallback: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "DuckDuckGo",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    resultsLabel(results.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(results, key = { _, result -> result.url }) { index, result ->
            DuckResultCard(
                result = result,
                index = index,
                onClick = { onOpenResult(result.url) },
            )
        }
        item(key = "footer") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Источник — DuckDuckGo. Реклама не показывается.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = onOpenFallback,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        "Открыть сайт",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
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
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SiteBadge(url = result.url, size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    host,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    BrowserIcons.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                result.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (result.snippet.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    result.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun resultsLabel(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "$count результат"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "$count результата"
    else -> "$count результатов"
}
