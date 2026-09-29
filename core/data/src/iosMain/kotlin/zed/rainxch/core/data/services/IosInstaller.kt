package zed.rainxch.core.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zed.rainxch.core.data.utils.presentShareSheetForFile
import zed.rainxch.core.domain.model.GithubAsset
import zed.rainxch.core.domain.model.SystemArchitecture
import zed.rainxch.core.domain.system.Installer
import zed.rainxch.core.domain.system.InstallerInfoExtractor

/**
 * iOS does not allow apps to install other apps. Downloaded `.ipa` files are handed to
 * the system share sheet so the user can open them in a sideloading tool
 * (AltStore, SideStore, TrollStore, ...) or save them to Files.
 */
class IosInstaller(
    private val installerInfoExtractor: InstallerInfoExtractor = IosInstallerInfoExtractor(),
) : Installer {
    override suspend fun isSupported(extOrMime: String): Boolean = extOrMime.lowercase().removePrefix(".") == "ipa"

    override suspend fun ensurePermissionsOrThrow(extOrMime: String) = Unit

    override suspend fun install(
        filePath: String,
        extOrMime: String,
    ) {
        withContext(Dispatchers.Main) {
            check(presentShareSheetForFile(filePath)) { "Unable to present the share sheet" }
        }
    }

    override fun uninstall(packageName: String) = Unit

    override fun isAssetInstallable(assetName: String): Boolean = assetName.lowercase().endsWith(".ipa")

    override fun choosePrimaryAsset(assets: List<GithubAsset>): GithubAsset? =
        assets
            .filter { isAssetInstallable(it.name) }
            .maxByOrNull { it.size }

    override fun detectSystemArchitecture(): SystemArchitecture = SystemArchitecture.AARCH64

    override fun isObtainiumInstalled(): Boolean = false

    override fun openInObtainium(
        repoOwner: String,
        repoName: String,
        onOpenInstaller: () -> Unit,
    ) = Unit

    override fun isAppManagerInstalled(): Boolean = false

    override fun openInAppManager(
        filePath: String,
        onOpenInstaller: () -> Unit,
    ) = Unit

    override fun getApkInfoExtractor(): InstallerInfoExtractor = installerInfoExtractor

    override fun openApp(packageName: String): Boolean = false

    override fun openWithExternalInstaller(filePath: String) {
        presentShareSheetForFile(filePath)
    }
}
