package zed.rainxch.core.data.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import zed.rainxch.core.domain.utils.BrowserHelper

class IosBrowserHelper : BrowserHelper {
    override fun openUrl(
        url: String,
        onFailure: (error: String) -> Unit,
    ) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            onFailure("Invalid URL: $url")
            return
        }
        UIApplication.sharedApplication.openURL(
            nsUrl,
            options = emptyMap<Any?, Any?>(),
            completionHandler = { success ->
                if (!success) onFailure("Failed to open browser. Please visit: $url")
            },
        )
    }
}
