package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.SiteBadge
import app.mimo.browser.web.TabState
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserCompactBar(
    state: TabState,
    tabCount: Int,
    onOpenOmnibox: () -> Unit,
    onTabs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalMimoHaptics.current
    val host = UrlKit.host(state.url)
        .ifBlank { state.displayHost }
        .ifBlank { state.displayTitle }
        .ifBlank { "Страница" }
    Surface(
        onClick = {
            haptics.tap()
            onOpenOmnibox()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 6.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.favicon != null) {
                SiteBadge(
                    url = state.url,
                    favicon = state.favicon,
                    size = 32.dp,
                )
            } else {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            BrowserIcons.Globe,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(9.dp))
            Text(
                host,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            TabCountBadge(count = tabCount, onClick = onTabs)
        }
    }
}

@Composable
fun TabCountBadge(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalMimoHaptics.current
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        modifier = modifier
            .height(28.dp)
            .widthIn(min = 28.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (count > 99) "99" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 7.dp),
            )
        }
    }
}
