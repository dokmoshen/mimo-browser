package app.mimo.browser.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ru = Locale("ru", "RU")

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return ""
    val units = listOf("Б", "КБ", "МБ", "ГБ")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index++
    }
    return if (index == 0) {
        "${value.toInt()} ${units[index]}"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[index])
    }
}

fun formatTime(epoch: Long): String =
    SimpleDateFormat("HH:mm", ru).format(Date(epoch))

fun dayStart(epoch: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = epoch
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun isToday(epoch: Long): Boolean {
    val now = System.currentTimeMillis()
    return dayStart(epoch) == dayStart(now)
}

fun isYesterday(epoch: Long): Boolean {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return dayStart(epoch) == dayStart(calendar.timeInMillis)
}

fun formatShortDate(epoch: Long): String =
    SimpleDateFormat("d MMM", ru).format(Date(epoch))

fun dateGroupLabel(epoch: Long): String = when {
    isToday(epoch) -> "Сегодня"
    isYesterday(epoch) -> "Вчера"
    else -> formatShortDate(epoch)
}

fun pluralRu(count: Int, one: String, few: String, many: String): String {
    val mod100 = kotlin.math.abs(count) % 100
    if (mod100 in 11..14) return many
    return when (mod100 % 10) {
        1 -> one
        2, 3, 4 -> few
        else -> many
    }
}
