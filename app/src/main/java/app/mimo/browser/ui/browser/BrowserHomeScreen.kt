package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.components.staggeredReveal
import app.mimo.browser.ui.theme.Unbounded

@Composable
fun BrowserHomeScreen(onOpenOmnibox: () -> Unit) {
    MimoBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-36).dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MimoLogo(
                modifier = Modifier
                    .size(76.dp)
                    .staggeredReveal(
                        index = 0,
                        offsetY = 22.dp,
                        initialScale = 0.9f,
                    ),
                color = MaterialTheme.colorScheme.primary,
                accent = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.staggeredReveal(
                    index = 1,
                    offsetY = 16.dp,
                ),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    "mimo",
                    fontFamily = Unbounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "browser",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(Modifier.height(36.dp))
            Box(
                Modifier.staggeredReveal(
                    index = 2,
                    offsetY = 24.dp,
                ),
            ) {
                HomeSearchBar(onClick = onOpenOmnibox)
            }
        }
    }
}

@Composable
private fun HomeSearchBar(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .pressScale(interaction, 0.965f),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp),
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
            Text(
                "Поиск или адрес",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
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
