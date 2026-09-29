package zed.rainxch.core.data.services

import co.touchlab.kermit.Logger
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import zed.rainxch.core.data.network.ProxyManager
import zed.rainxch.core.data.network.createPlatformHttpClient
import zed.rainxch.core.domain.model.DownloadProgress
import zed.rainxch.core.domain.network.Downloader
import kotlin.random.Random

class IosDownloader(
    private val files: FileLocationsProvider,
    private val proxyManager: ProxyManager = ProxyManager,
) : Downloader {
    private val fileSystem = FileSystem.SYSTEM
    private val mutex = Mutex()
    private val activeDownloads = mutableMapOf<String, Job>()

    override fun download(
        url: String,
        suggestedFileName: String?,
    ): Flow<DownloadProgress> =
        channelFlow {
            val safeName = resolveFileName(url, suggestedFileName)
            val destination = destinationFor(safeName)

            val job = currentCoroutineContext()[Job]
            mutex.withLock {
                check(safeName !in activeDownloads) { "A download for '$safeName' is already in progress" }
                if (job != null) activeDownloads[safeName] = job
            }

            val client = createPlatformHttpClient(proxyManager.currentProxyConfig.value)
            try {
                fileSystem.delete(destination)
                Logger.d { "Starting download: $url" }

                client.prepareGet(url).execute { response ->
                    if (!response.status.isSuccess()) {
                        throw IOException("Unexpected code ${response.status.value}")
                    }
                    val total = response.contentLength()?.takeIf { it > 0 }
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L

                    fileSystem.sink(destination).buffer().use { sink ->
                        while (true) {
                            val read = channel.readAvailable(buffer, 0, buffer.size)
                            if (read == -1) break
                            if (read == 0) continue
                            sink.write(buffer, 0, read)
                            downloaded += read
                            send(DownloadProgress(downloaded, total, total?.let { ((downloaded * 100L) / it).toInt() }))
                        }
                    }

                    val size = fileSystem.metadataOrNull(destination)?.size ?: 0L
                    check(size > 0) { "File not ready after download: $destination" }
                    send(DownloadProgress(size, total, total?.let { ((size * 100L) / it).toInt() } ?: 100))
                }
            } catch (e: Exception) {
                fileSystem.delete(destination)
                Logger.e(e) { "Download failed" }
                throw e
            } finally {
                client.close()
                mutex.withLock { activeDownloads.remove(safeName) }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun saveToFile(
        url: String,
        suggestedFileName: String?,
    ): String {
        val safeName = resolveFileName(url, suggestedFileName)
        download(url, suggestedFileName).collect { }
        return destinationFor(safeName).toString()
    }

    override suspend fun getDownloadedFilePath(fileName: String): String? =
        withContext(Dispatchers.IO) {
            val path = destinationFor(fileName)
            val size = fileSystem.metadataOrNull(path)?.size ?: 0L
            if (size > 0) path.toString() else null
        }

    override suspend fun cancelDownload(fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            val job = mutex.withLock { activeDownloads.remove(fileName) }
            job?.cancel()
            val path = destinationFor(fileName)
            val existed = fileSystem.exists(path)
            fileSystem.delete(path)
            job != null || existed
        }

    private fun destinationFor(fileName: String): Path = "${files.userDownloadsDir()}/$fileName".toPath()

    private fun resolveFileName(
        url: String,
        suggestedFileName: String?,
    ): String {
        val rawName =
            suggestedFileName?.takeIf { it.isNotBlank() }
                ?: url
                    .substringAfterLast('/')
                    .substringBefore('?')
                    .substringBefore('#')
                    .ifBlank { "asset-${Random.nextLong().toULong()}" }
        val safeName = rawName.substringAfterLast('/').substringAfterLast('\\')
        require(safeName.isNotBlank() && safeName != "." && safeName != "..") {
            "Invalid file name: $rawName"
        }
        return safeName
    }

    private companion object {
        const val DEFAULT_BUFFER_SIZE = 8 * 1024
    }
}
