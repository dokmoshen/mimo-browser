package app.mimo.browser.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder

class HistoryRepository(private val dao: HistoryDao) {
    val recent: Flow<List<HistoryEntity>> = dao.observeRecent(30)
    val all: Flow<List<HistoryEntity>> = dao.observeAll()
    val count: Flow<Int> = dao.observeCount()

    fun search(query: String): Flow<List<HistoryEntity>> = dao.search(query, 40)

    suspend fun recordVisit(url: String, title: String) = withContext(Dispatchers.IO) {
        if (!url.startsWith("http")) return@withContext
        val cleanTitle = title.trim().ifBlank { hostOf(url) }
        val now = System.currentTimeMillis()
        val updated = dao.touch(url, cleanTitle, now)
        if (updated == 0) {
            dao.insert(HistoryEntity(url = url, title = cleanTitle, visitedAt = now))
        }
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) { dao.delete(id) }

    suspend fun clear() = withContext(Dispatchers.IO) { dao.clear() }

    private fun hostOf(url: String): String = runCatching {
        Uri.parse(url).host?.removePrefix("www.").orEmpty().ifBlank { url }
    }.getOrDefault(url)
}

class BookmarkRepository(private val dao: BookmarkDao) {
    val all: Flow<List<BookmarkEntity>> = dao.observeAll()
    val count: Flow<Int> = dao.observeCount()

    fun isBookmarked(url: String): Flow<Boolean> = dao.observe(url)

    fun search(query: String): Flow<List<BookmarkEntity>> = dao.search(query, 40)

    suspend fun toggle(url: String, title: String): Boolean = withContext(Dispatchers.IO) {
        if (!url.startsWith("http")) return@withContext false
        val existing = dao.find(url)
        if (existing != null) {
            dao.delete(existing.id)
            false
        } else {
            dao.insert(
                BookmarkEntity(
                    url = url,
                    title = title.trim().ifBlank { Uri.parse(url).host ?: url },
                    createdAt = System.currentTimeMillis(),
                ),
            )
            true
        }
    }

    suspend fun remove(id: Long) = withContext(Dispatchers.IO) { dao.delete(id) }

    suspend fun removeByUrl(url: String) = withContext(Dispatchers.IO) { dao.deleteByUrl(url) }

    suspend fun clear() = withContext(Dispatchers.IO) { dao.clear() }
}

object DownloadStatus {
    const val RUNNING = "RUNNING"
    const val COMPLETE = "COMPLETE"
    const val FAILED = "FAILED"
}

class DownloadRepository(
    private val context: Context,
    private val dao: DownloadDao,
) {
    private val manager: DownloadManager
        get() = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val all: Flow<List<DownloadEntity>> = dao.observeAll()
    val count: Flow<Int> = dao.observeCount()

    suspend fun enqueue(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
    ): Long? = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = uniqueFileName(fileNameFor(url, contentDisposition, mimeType))
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription(Uri.parse(url).host ?: fileName)
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                )
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName,
                )
                if (!userAgent.isNullOrBlank()) addRequestHeader("User-Agent", userAgent)
                resolveMime(fileName, mimeType)?.let { setMimeType(it) }
            }
            val id = manager.enqueue(request)
            val path = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName,
            ).absolutePath
            dao.insert(
                DownloadEntity(
                    downloadId = id,
                    url = url,
                    fileName = fileName,
                    mimeType = resolveMime(fileName, mimeType),
                    totalBytes = contentLength.coerceAtLeast(0),
                    createdAt = System.currentTimeMillis(),
                    status = DownloadStatus.RUNNING,
                    localPath = path,
                ),
            )
            id
        }.getOrNull()
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        dao.all().forEach { download ->
            if (download.status != DownloadStatus.RUNNING) return@forEach
            val cursor = manager.query(DownloadManager.Query().setFilterById(download.downloadId))
            cursor.use {
                if (!it.moveToFirst()) {
                    dao.updateStatus(download.id, DownloadStatus.FAILED, download.localPath)
                    return@forEach
                }
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL ->
                        dao.updateStatus(download.id, DownloadStatus.COMPLETE, download.localPath)
                    DownloadManager.STATUS_FAILED ->
                        dao.updateStatus(download.id, DownloadStatus.FAILED, download.localPath)
                }
            }
        }
    }

    suspend fun remove(download: DownloadEntity) = withContext(Dispatchers.IO) {
        runCatching { manager.remove(download.downloadId) }
        download.localPath?.let { runCatching { File(it).delete() } }
        dao.delete(download.id)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        dao.all().forEach { download ->
            runCatching { manager.remove(download.downloadId) }
            download.localPath?.let { runCatching { File(it).delete() } }
        }
        dao.clear()
    }

    fun open(download: DownloadEntity) {
        val file = download.localPath?.let(::File)
        if (file == null || !file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, download.mimeType ?: "application/octet-stream")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, download.fileName))
        }
    }

    fun share(download: DownloadEntity) {
        val file = download.localPath?.let(::File)
        if (file == null || !file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = download.mimeType ?: "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, download.fileName))
        }
    }

    fun fileExists(download: DownloadEntity): Boolean =
        download.localPath?.let { File(it).exists() } == true

    private fun uniqueFileName(name: String): String {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return name
        var candidate = name
        if (!File(dir, candidate).exists()) return candidate
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var index = 2
        while (File(dir, "$base ($index)$ext").exists()) index++
        candidate = "$base ($index)$ext"
        return candidate
    }

    private fun fileNameFor(url: String, disposition: String?, mime: String?): String {
        disposition?.let { value ->
            val encoded = Regex("""filename\*=UTF-8''([^;]+)""", RegexOption.IGNORE_CASE)
                .find(value)?.groupValues?.getOrNull(1)
            val plain = Regex("""filename="?([^";]+)"?""", RegexOption.IGNORE_CASE)
                .find(value)?.groupValues?.getOrNull(1)
            val raw = encoded?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() } ?: plain
            if (!raw.isNullOrBlank()) return sanitizeName(raw)
        }
        val last = Uri.parse(url).lastPathSegment?.substringAfterLast('/')
        if (!last.isNullOrBlank()) {
            val decoded = runCatching { URLDecoder.decode(last, "UTF-8") }.getOrDefault(last)
            if (decoded.contains('.')) return sanitizeName(decoded)
        }
        val ext = mime?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?: "bin"
        val host = Uri.parse(url).host?.substringBefore('.') ?: "download"
        return sanitizeName("$host-${System.currentTimeMillis()}.$ext")
    }

    private fun sanitizeName(name: String): String {
        val clean = name
            .substringAfterLast('/')
            .replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001f]"), "_")
            .trim()
            .take(120)
        return clean.ifBlank { "download-${System.currentTimeMillis()}" }
    }

    private fun resolveMime(fileName: String, declared: String?): String? {
        if (!declared.isNullOrBlank() && declared != "application/octet-stream") return declared
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: declared
    }
}
