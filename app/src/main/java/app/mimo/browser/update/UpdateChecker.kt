package app.mimo.browser.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.math.max

data class UpdateInfo(
    val version: String,
    val tag: String,
    val notes: String,
    val assetUrl: String,
    val size: Long,
    val sha256: String?,
)

/**
 * Checks the public GitHub release only when the user asks, verifies the
 * download and hands the APK to the system installer.
 */
object UpdateChecker {

    private const val API = "https://api.github.com/repos/dokmoshen/mimo-browser/releases/latest"
    private const val ASSET_NAME = "mimo-browser.apk"
    private const val USER_AGENT = "Mimo-Browser-Android"

    suspend fun check(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        val connection = (URL(API).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "$USER_AGENT/$currentVersion")
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw IOException("GitHub HTTP $code")
            }
            val root = JSONObject(body)
            val tag = root.optString("tag_name").trim()
            val version = tag.removePrefix("v").removePrefix("V")
            if (!isNewer(version, currentVersion)) return@withContext null

            val assets = root.optJSONArray("assets") ?: return@withContext null
            var asset: JSONObject? = null
            for (index in 0 until assets.length()) {
                val candidate = assets.optJSONObject(index) ?: continue
                if (candidate.optString("name") == ASSET_NAME) {
                    asset = candidate
                    break
                }
            }
            val selected = asset ?: return@withContext null
            val url = selected.optString("browser_download_url")
            if (url.isBlank()) return@withContext null
            val digest = selected.optString("digest")
                .removePrefix("sha256:")
                .takeIf { it.length == 64 }
            UpdateInfo(
                version = version,
                tag = tag,
                notes = root.optString("body").trim(),
                assetUrl = url,
                size = selected.optLong("size", 0L),
                sha256 = digest,
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun download(
        context: Context,
        info: UpdateInfo,
        onProgress: suspend (downloaded: Long, total: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val target = File(dir, "mimo-browser-${info.version}.apk")
        val temp = File(dir, "${target.name}.part")
        val connection = (URL(info.assetUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("User-Agent", "$USER_AGENT/${info.version}")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("Download HTTP $code")
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: info.size
            val digest = MessageDigest.getInstance("SHA-256")
            var downloaded = 0L
            connection.inputStream.use { input ->
                FileOutputStream(temp).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded, total)
                    }
                }
            }
            val actual = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
            if (info.sha256 != null && !actual.equals(info.sha256, ignoreCase = true)) {
                temp.delete()
                throw IOException("Checksum mismatch")
            }
            if (!temp.renameTo(target)) {
                temp.delete()
                throw IOException("Could not store update")
            }
            verifyArchive(context, target)
            target
        } finally {
            connection.disconnect()
            if (temp.exists()) temp.delete()
        }
    }

    fun canInstall(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    fun openInstallSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun install(context: Context, file: File): Boolean {
        if (!canInstall(context)) {
            openInstallSettings(context)
            return false
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    private fun verifyArchive(context: Context, file: File) {
        val manager = context.packageManager
        val archive = manager.getPackageArchiveInfo(
            file.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES,
        ) ?: throw IOException("Not an APK")
        if (archive.packageName != context.packageName) {
            throw IOException("Wrong package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val installed = manager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            ).signingInfo
            val update = archive.signingInfo
            if (installed != null && update != null) {
                val previous = installed.apkContentsSigners.toList()
                val history = update.signingCertificateHistory.toList()
                if (previous.isNotEmpty() && history.none { new ->
                        previous.any { old -> old == new }
                    }
                ) {
                    throw IOException("Signature mismatch")
                }
            }
        }
    }

    internal fun isNewer(remote: String, current: String): Boolean {
        val remoteParts = remote.split('.').mapNotNull { it.toIntOrNull() }
        val currentParts = current.split('.').mapNotNull { it.toIntOrNull() }
        if (remoteParts.isEmpty()) return false
        for (index in 0 until max(remoteParts.size, currentParts.size)) {
            val next = remoteParts.getOrElse(index) { 0 }
            val installed = currentParts.getOrElse(index) { 0 }
            if (next != installed) return next > installed
        }
        return false
    }
}
