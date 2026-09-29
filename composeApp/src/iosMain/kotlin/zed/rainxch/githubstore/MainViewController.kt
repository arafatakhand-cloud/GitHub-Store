package zed.rainxch.githubstore

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow
import platform.UIKit.UIViewController
import zed.rainxch.githubstore.app.di.initKoin

private val deepLinkUri = MutableStateFlow<String?>(null)
private var isKoinStarted = false

/**
 * Entry point used by the Xcode project (`iosApp/iosApp/ContentView.swift`).
 */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    if (!isKoinStarted) {
        initKoin()
        isKoinStarted = true
    }
    return ComposeUIViewController {
        val uri by deepLinkUri.collectAsState()
        App(deepLinkUri = uri)
    }
}

/**
 * Forwards `githubstore://` URLs received by the iOS app (see `onOpenURL` in `iOSApp.swift`).
 */
@Suppress("unused")
fun handleDeepLink(url: String) {
    deepLinkUri.value = url
}
