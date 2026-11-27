package app.mimo.browser.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mimo.browser.BuildConfig
import app.mimo.browser.preferences.BrowserSettings
import app.mimo.browser.preferences.DesignLanguage
import app.mimo.browser.preferences.SearchEngine
import app.mimo.browser.preferences.ThemeMode
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.AnimatedMimoMark
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoChip
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.ui.components.SectionHeader
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.displayName
import app.mimo.browser.ui.util.pluralRu

private enum class ClearTarget { HISTORY, BOOKMARKS, DOWNLOADS, WEB_DATA }

@Composable
fun BrowserSettingsScreen(
    settings: BrowserSettings,
    historyCount: Int,
    bookmarkCount: Int,
    downloadCount: Int,
    onBack: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onDesignLanguage: (DesignLanguage) -> Unit,
    onSearchEngine: (SearchEngine) -> Unit,
    onJavascript: (Boolean) -> Unit,
    onDesktopMode: (Boolean) -> Unit,
    onInterfaceBlur: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onClearBookmarks: () -> Unit,
    onClearDownloads: () -> Unit,
    onClearWebData: () -> Unit,
) {
    var clearTarget by remember { mutableStateOf<ClearTarget?>(null) }
    val haptics = LocalMimoHaptics.current

    MimoBackdrop {
        Column(
            Modifier
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
                        "Настройки",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "тёплый браузер — ваши правила",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Оформление")
                SettingsCard {
                    Text(
                        "Тема",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChip("Системная", settings.themeMode == ThemeMode.SYSTEM) {
                            onTheme(ThemeMode.SYSTEM)
                        }
                        ThemeChip("Светлая", settings.themeMode == ThemeMode.LIGHT) {
                            onTheme(ThemeMode.LIGHT)
                        }
                        ThemeChip("Тёмная", settings.themeMode == ThemeMode.DARK) {
                            onTheme(ThemeMode.DARK)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    if (settings.designLanguage == DesignLanguage.EXPRESSIVE) {
                        SettingSwitchRow(
                            title = "Динамические цвета",
                            subtitle = "Подстраивать палитру под обои",
                            checked = settings.dynamicColor,
                            onCheckedChange = onDynamicColor,
                            icon = MimoIcons.Palette,
                        )
                    }
                    SettingSwitchRow(
                        title = "Стеклянные поверхности",
                        subtitle = "Мягкие полупрозрачные панели",
                        checked = settings.interfaceBlur,
                        onCheckedChange = onInterfaceBlur,
                        icon = MimoIcons.BlurOn,
                    )
                }
                SectionHeader("Язык дизайна")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DesignCard(
                        title = "Expressive",
                        subtitle = "Тёплая бумага, мягкие формы, пружины.",
                        selected = settings.designLanguage == DesignLanguage.EXPRESSIVE,
                        onClick = { onDesignLanguage(DesignLanguage.EXPRESSIVE) },
                        modifier = Modifier.weight(1f),
                    )
                    DesignCard(
                        title = "Nothing",
                        subtitle = "Монохром, красный акцент, точка.",
                        selected = settings.designLanguage == DesignLanguage.NOTHING,
                        onClick = { onDesignLanguage(DesignLanguage.NOTHING) },
                        modifier = Modifier.weight(1f),
                    )
                }
                SectionHeader("Просмотр")
                SettingsCard {
                    Text(
                        "Поиск по умолчанию",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    SearchEngine.entries.toList().chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEach { engine ->
                                SearchEngineChip(
                                    engine = engine,
                                    selected = settings.searchEngine == engine,
                                    onClick = { onSearchEngine(engine) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    SettingSwitchRow(
                        title = "JavaScript",
                        subtitle = "Без него часть сайтов не работает",
                        checked = settings.javascriptEnabled,
                        onCheckedChange = onJavascript,
                        icon = BrowserIcons.Globe,
                    )
                    SettingSwitchRow(
                        title = "Версия для компьютера",
                        subtitle = "Открывать сайты в настольном виде",
                        checked = settings.desktopMode,
                        onCheckedChange = onDesktopMode,
                        icon = BrowserIcons.Desktop,
                    )
                }
                SectionHeader("Данные")
                SettingsCard {
                    SettingActionRow(
                        title = "История",
                        subtitle = if (historyCount == 0) "Пусто" else "$historyCount ${pluralRu(historyCount, "запись", "записи", "записей")}",
                        icon = MimoIcons.History,
                        onClick = { clearTarget = ClearTarget.HISTORY },
                    )
                    SettingActionRow(
                        title = "Закладки",
                        subtitle = if (bookmarkCount == 0) "Пусто" else "$bookmarkCount ${pluralRu(bookmarkCount, "закладка", "закладки", "закладок")}",
                        icon = BrowserIcons.Bookmark,
                        onClick = { clearTarget = ClearTarget.BOOKMARKS },
                    )
                    SettingActionRow(
                        title = "Загрузки",
                        subtitle = if (downloadCount == 0) "Пусто" else "$downloadCount ${pluralRu(downloadCount, "файл", "файла", "файлов")}",
                        icon = MimoIcons.FileDownload,
                        onClick = { clearTarget = ClearTarget.DOWNLOADS },
                    )
                    SettingActionRow(
                        title = "Cookies и данные сайтов",
                        subtitle = "Выйти из аккаунтов и очистить кэш",
                        icon = MimoIcons.Storage,
                        onClick = { clearTarget = ClearTarget.WEB_DATA },
                    )
                }
                SectionHeader("О приложении")
                AboutCard()
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    clearTarget?.let { target ->
        val title: String
        val body: String
        when (target) {
            ClearTarget.HISTORY -> {
                title = "Очистить историю?"
                body = "Все посещения исчезнут. Это действие нельзя отменить."
            }
            ClearTarget.BOOKMARKS -> {
                title = "Очистить закладки?"
                body = "Все сохранённые страницы исчезнут. Это действие нельзя отменить."
            }
            ClearTarget.DOWNLOADS -> {
                title = "Очистить загрузки?"
                body = "Список загрузок и скачанные файлы будут удалены."
            }
            ClearTarget.WEB_DATA -> {
                title = "Очистить данные сайтов?"
                body = "Cookies, кэш и формы будут удалены. Возможно, придётся заново войти на сайты."
            }
        }
        AlertDialog(
            onDismissRequest = { clearTarget = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(onClick = {
                    clearTarget = null
                    haptics.confirm()
                    when (target) {
                        ClearTarget.HISTORY -> onClearHistory()
                        ClearTarget.BOOKMARKS -> onClearBookmarks()
                        ClearTarget.DOWNLOADS -> onClearDownloads()
                        ClearTarget.WEB_DATA -> onClearWebData()
                    }
                }) { Text("Очистить") }
            },
            dismissButton = {
                TextButton(onClick = { clearTarget = null }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    MimoChip(label = label, selected = selected, onClick = onClick)
}

@Composable
private fun SearchEngineChip(
    engine: SearchEngine,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalMimoHaptics.current
    Surface(
        onClick = {
            haptics.toggle(selected)
            onClick()
        },
        modifier = modifier.height(42.dp),
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (selected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                engine.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 44.dp, height = 28.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
        )
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val haptics = LocalMimoHaptics.current
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .pressScale(interaction, 0.99f),
        shape = RoundedCornerShape(16.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                MimoIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DesignCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalMimoHaptics.current
    Surface(
        onClick = {
            haptics.toggle(selected)
            onClick()
        },
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            if (selected) 1.6.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(24.dp)
                        .then(if (selected) Modifier else Modifier),
                ) {
                    if (title == "Nothing") {
                        Text(
                            "|",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        MimoLogo(
                            Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.primary,
                            accent = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (selected) {
                    Icon(
                        MimoIcons.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun AboutCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedMimoMark(
                modifier = Modifier.size(58.dp),
                color = MaterialTheme.colorScheme.primary,
                accent = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Mimo Browser",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Версия ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Тёплый браузер на Material 3 Expressive. Никаких аккаунтов и облака: история, закладки и настройки остаются на устройстве.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "GPL-3.0 · основан на проекте Mimo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
