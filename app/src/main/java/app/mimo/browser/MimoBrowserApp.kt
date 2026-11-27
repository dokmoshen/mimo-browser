package app.mimo.browser

import android.content.Context
import app.mimo.browser.data.BookmarkRepository
import app.mimo.browser.data.BrowserDatabase
import app.mimo.browser.data.DownloadRepository
import app.mimo.browser.data.HistoryRepository
import app.mimo.browser.preferences.BrowserPreferences

class AppContainer(context: Context) {
    private val database = BrowserDatabase.get(context)
    val history = HistoryRepository(database.historyDao())
    val bookmarks = BookmarkRepository(database.bookmarkDao())
    val downloads = DownloadRepository(context, database.downloadDao())
    val preferences = BrowserPreferences(context)
}

class MimoBrowserApp : android.app.Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
