package zed.rainxch.core.data.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

internal fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/**
 * Presents the system share sheet. On iPad the sheet is a popover and needs an anchor,
 * otherwise UIKit throws, so it is anchored to the centre of the presenting view.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun presentShareSheet(items: List<Any>): Boolean {
    val presenter = topViewController() ?: return false
    val activity = UIActivityViewController(activityItems = items, applicationActivities = null)
    activity.popoverPresentationController?.let { popover ->
        popover.sourceView = presenter.view
        presenter.view.bounds.useContents {
            popover.sourceRect = CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0)
        }
    }
    presenter.presentViewController(activity, animated = true, completion = null)
    return true
}

internal fun presentShareSheetForFile(path: String): Boolean = presentShareSheet(listOf(NSURL.fileURLWithPath(path)))
