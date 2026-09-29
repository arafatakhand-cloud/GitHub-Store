package zed.rainxch.core.data.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun directoryPath(directory: NSSearchPathDirectory): String {
    val url: NSURL? =
        NSFileManager.defaultManager.URLForDirectory(
            directory = directory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
    return requireNotNull(url?.path) { "Could not resolve iOS directory $directory" }
}

internal fun iosDocumentsDir(): String = directoryPath(NSDocumentDirectory)

internal fun iosCachesDir(): String = directoryPath(NSCachesDirectory)
