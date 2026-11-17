package com.drskur.vocabean.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class AppUpdate(val versionCode: Long, val versionName: String, val notes: String, val url: String, val sha256: String)

/** Only signed APKs from this repository's HTTPS release assets are accepted. */
class AppUpdater(private val context: Context) {
    private val releasePrefix = "https://github.com/drskur/voca-bean/releases/download/"

    suspend fun check(): AppUpdate? = withContext(Dispatchers.IO) {
        val release = JSONObject(get("https://api.github.com/repos/drskur/voca-bean/releases/latest") {
            it.bufferedReader().use { reader -> reader.readText() }
        })
        val assets = release.getJSONArray("assets")
        val manifest = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.getString("name") == "update.json" } ?: return@withContext null
        val metadataUrl = manifest.getString("browser_download_url")
        require(metadataUrl.startsWith(releasePrefix))
        val metadata = JSONObject(get(metadataUrl) { it.bufferedReader().use { reader -> reader.readText() } })
        val code = metadata.getLong("versionCode")
        val installed = context.packageManager.getPackageInfo(context.packageName, 0)
        if (code <= installed.longVersionCode) return@withContext null
        val apkName = metadata.getString("apkFile")
        val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.getString("name") == apkName } ?: error("업데이트 APK가 없습니다.")
        val url = apk.getString("browser_download_url")
        require(url.startsWith(releasePrefix) && apkName.endsWith(".apk"))
        val hash = metadata.getString("sha256").lowercase()
        require(hash.matches(Regex("[0-9a-f]{64}")))
        AppUpdate(code, metadata.getString("versionName"), metadata.optString("notes"), url, hash)
    }

    suspend fun download(update: AppUpdate): File = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "updates").apply { mkdirs() }
        // Unique files avoid overwriting a pending installation.
        val file = File.createTempFile("update-", ".apk", directory)
        get(update.url) { input ->
            file.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 200L * 1024 * 1024) { "APK 크기 제한을 초과했습니다." }
                    output.write(buffer, 0, count)
                }
            }
        }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        require(digest.digest().joinToString("") { "%02x".format(it) } == update.sha256) { "APK 무결성 검증에 실패했습니다." }
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val installed = context.packageManager.getPackageInfo(context.packageName, flags)
        val archive = context.packageManager.getPackageArchiveInfo(file.absolutePath, flags)
            ?: error("올바른 APK가 아닙니다.")
        require(archive.packageName == context.packageName && archive.longVersionCode == update.versionCode &&
            archive.longVersionCode > installed.longVersionCode) { "APK 버전 또는 패키지가 일치하지 않습니다." }
        val currentSigners = installed.signingInfo?.apkContentsSigners?.toSet()
        val newSigners = archive.signingInfo?.apkContentsSigners?.toSet()
        require(!currentSigners.isNullOrEmpty() && currentSigners == newSigners) { "APK 서명이 설치된 앱과 다릅니다." }
        file
    }

    fun install(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun <T> get(url: String, block: (java.io.InputStream) -> T): T {
        require(URL(url).protocol == "https")
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("User-Agent", "VocaBean-Android")
        try {
            if (connection.responseCode == 404) error("아직 배포된 업데이트가 없습니다.")
            check(connection.responseCode in 200..299) { "업데이트 서버 오류: ${connection.responseCode}" }
            require(connection.url.protocol == "https")
            return connection.inputStream.use(block)
        } finally {
            connection.disconnect()
        }
    }
}
