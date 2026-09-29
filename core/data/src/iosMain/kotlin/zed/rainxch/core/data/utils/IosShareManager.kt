package zed.rainxch.core.data.utils

import co.touchlab.kermit.Logger
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject
import zed.rainxch.core.domain.utils.ShareManager

class IosShareManager : ShareManager {
    // UIDocumentPickerViewController only keeps a weak reference to its delegate.
    private var pickerDelegate: PickerDelegate? = null

    override fun shareText(text: String) {
        presentShareSheet(listOf(text))
    }

    override fun shareFile(
        fileName: String,
        content: String,
        mimeType: String,
    ) {
        val path = "${iosCachesDir()}/$fileName".toPath()
        try {
            FileSystem.SYSTEM.write(path) { writeUtf8(content) }
            presentShareSheetForFile(path.toString())
        } catch (e: Exception) {
            Logger.e(e) { "Failed to share $fileName" }
        }
    }

    override fun pickFile(
        mimeType: String,
        onResult: (String?) -> Unit,
    ) {
        val presenter = topViewController()
        if (presenter == null) {
            onResult(null)
            return
        }
        val delegate =
            PickerDelegate { url ->
                pickerDelegate = null
                onResult(url?.let(::readPickedFile))
            }
        pickerDelegate = delegate
        val picker =
            UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeJSON),
                asCopy = true,
            )
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    private fun readPickedFile(url: NSURL): String? {
        val path = url.path ?: return null
        val scoped = url.startAccessingSecurityScopedResource()
        return try {
            FileSystem.SYSTEM.read(path.toPath()) { readUtf8() }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to read picked file" }
            null
        } finally {
            if (scoped) url.stopAccessingSecurityScopedResource()
        }
    }

    private class PickerDelegate(
        private val onPicked: (NSURL?) -> Unit,
    ) : NSObject(),
        UIDocumentPickerDelegateProtocol {
        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>,
        ) {
            onPicked(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            onPicked(null)
        }
    }
}
