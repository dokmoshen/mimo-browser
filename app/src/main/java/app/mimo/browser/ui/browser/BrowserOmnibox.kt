package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.ui.OmniboxSuggestion
import app.mimo.browser.ui.SuggestionKind
import app.mimo.browser.ui.components.BrowserArt
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.BrowserIllustration
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoChip
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.ui.theme.Unbounded

private data class QuickDestination(val label: String, val url: String)

private val quickDestinations = listOf(
    QuickDestination("Википедия", "https://ru.wikipedia.org"),
    QuickDestination("YouTube", "https://m.youtube.com"),
    QuickDestination("GitHub", "https://github.com"),
    QuickDestination("Хабр", "https://habr.com"),
)

@Composable
fun BrowserOmnibox(
    query: String,
    suggestions: List<OmniboxSuggestion>,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenSuggestion: (OmniboxSuggestion) -> Unit,
    onOpenUrl: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    MimoBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        MimoIcons.ArrowBack,
                        contentDescription = "Закрыть",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                    ),
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            MimoIcons.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(21.dp),
                        )
                        Spacer(Modifier.width(11.dp))
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    "Введите адрес или запрос",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        onSubmit()
                                        keyboard?.hide()
                                    },
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                            )
                        }
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    MimoIcons.Close,
                                    contentDescription = "Очистить",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            if (query.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Text(
                        "Быстрый старт",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickDestinations.take(2).forEach { destination ->
                            MimoChip(
                                label = destination.label,
                                selected = false,
                                onClick = { onOpenUrl(destination.url) },
                                leadingIcon = BrowserIcons.Globe,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickDestinations.drop(2).forEach { destination ->
                            MimoChip(
                                label = destination.label,
                                selected = false,
                                onClick = { onOpenUrl(destination.url) },
                                leadingIcon = BrowserIcons.Globe,
                            )
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (suggestions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            BrowserArt(
                                kind = BrowserIllustration.GLOBE,
                                modifier = Modifier.width(190.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (query.isBlank()) "Начните вводить адрес или запрос" else "Ничего не найдено",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                if (query.isBlank()) {
                                    "Mimo подскажет страницы из истории и закладок."
                                } else {
                                    "Можно всё равно нажать «Найти» — мы поищем в интернете."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp),
                            )
                        }
                    }
                } else {
                    if (query.isBlank()) {
                        item {
                            Text(
                                "Недавнее и закладки",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 6.dp),
                            )
                        }
                    }
                    items(suggestions, key = { it.kind.name + it.url + it.title }) { suggestion ->
                        SuggestionRow(
                            suggestion = suggestion,
                            query = query,
                            onClick = {
                                keyboard?.hide()
                                onOpenSuggestion(suggestion)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: OmniboxSuggestion,
    query: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (suggestion.kind) {
                SuggestionKind.SEARCH -> {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(MimoIcons.Search, contentDescription = null, modifier = Modifier.size(19.dp))
                        }
                    }
                }
                SuggestionKind.URL -> {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(BrowserIcons.Globe, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                SuggestionKind.BOOKMARK, SuggestionKind.HISTORY -> {
                    SiteBadge(url = suggestion.url, size = 40.dp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                if (suggestion.kind == SuggestionKind.SEARCH) {
                    Text(
                        text = buildAnnotatedString {
                            append("Искать ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("«${suggestion.title}»")
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "в ${suggestion.subtitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = suggestion.title.ifBlank { suggestion.subtitle },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (suggestion.kind == SuggestionKind.BOOKMARK) {
                            Icon(
                                BrowserIcons.StarFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(10.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            suggestion.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Icon(
                BrowserIcons.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
