package app.mimo.browser.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingTest {

    @Test
    fun russianPlural() {
        assertEquals("файл", pluralRu(1, "файл", "файла", "файлов"))
        assertEquals("файла", pluralRu(3, "файл", "файла", "файлов"))
        assertEquals("файлов", pluralRu(5, "файл", "файла", "файлов"))
        assertEquals("файлов", pluralRu(11, "файл", "файла", "файлов"))
        assertEquals("файл", pluralRu(21, "файл", "файла", "файлов"))
        assertEquals("файла", pluralRu(22, "файл", "файла", "файлов"))
    }

    @Test
    fun byteFormatting() {
        assertEquals("512 Б", formatBytes(512))
        assertEquals("1.0 КБ", formatBytes(1024))
        assertEquals("1.5 МБ", formatBytes(1024L * 1024L + 512L * 1024L))
    }
}
