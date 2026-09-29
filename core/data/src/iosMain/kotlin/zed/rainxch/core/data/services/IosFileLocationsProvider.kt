package zed.rainxch.core.data.services

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import zed.rainxch.core.data.utils.iosCachesDir
import zed.rainxch.core.data.utils.iosDocumentsDir

class IosFileLocationsProvider : FileLocationsProvider {
    private val fileSystem = FileSystem.SYSTEM

    override fun appDownloadsDir(): String = ensureDir("${iosCachesDir()}/Downloads".toPath())

    // Stored under Documents so downloads show up in the Files app
    // (requires UIFileSharingEnabled / LSSupportsOpeningDocumentsInPlace in Info.plist).
    override fun userDownloadsDir(): String = ensureDir("${iosDocumentsDir()}/GitHub Store Downloads".toPath())

    override fun setExecutableIfNeeded(path: String) {
        // Not applicable on iOS.
    }

    override fun getCacheSizeBytes(): Long =
        dirSize(appDownloadsDir().toPath()) + dirSize(userDownloadsDir().toPath())

    override fun clearCacheFiles(): Boolean =
        deleteContents(appDownloadsDir().toPath()) and deleteContents(userDownloadsDir().toPath())

    private fun ensureDir(path: Path): String {
        fileSystem.createDirectories(path)
        return path.toString()
    }

    private fun dirSize(dir: Path): Long =
        runCatching {
            fileSystem
                .listRecursively(dir)
                .sumOf { fileSystem.metadataOrNull(it)?.takeIf { m -> m.isRegularFile }?.size ?: 0L }
        }.getOrDefault(0L)

    private fun deleteContents(dir: Path): Boolean =
        runCatching {
            fileSystem.list(dir).forEach { fileSystem.deleteRecursively(it) }
        }.isSuccess
}
