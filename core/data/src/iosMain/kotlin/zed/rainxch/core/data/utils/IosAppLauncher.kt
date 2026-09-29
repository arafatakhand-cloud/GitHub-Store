package zed.rainxch.core.data.utils

import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.domain.utils.AppLauncher

/**
 * iOS sandboxing does not allow launching arbitrary apps by bundle id.
 */
class IosAppLauncher : AppLauncher {
    override suspend fun launchApp(installedApp: InstalledApp): Result<Unit> =
        Result.failure(UnsupportedOperationException("Launching other apps is not supported on iOS"))

    override suspend fun canLaunchApp(installedApp: InstalledApp): Boolean = false
}
