package zed.rainxch.core.data.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import zed.rainxch.core.domain.model.ApkPackageInfo
import zed.rainxch.core.domain.model.DeviceApp
import zed.rainxch.core.domain.model.ShizukuAvailability
import zed.rainxch.core.domain.model.SystemPackageInfo
import zed.rainxch.core.domain.system.InstallerInfoExtractor
import zed.rainxch.core.domain.system.InstallerStatusProvider
import zed.rainxch.core.domain.system.PackageMonitor
import zed.rainxch.core.domain.system.UpdateScheduleManager

/** iOS apps cannot inspect other installed apps. */
class IosPackageMonitor : PackageMonitor {
    override suspend fun isPackageInstalled(packageName: String): Boolean = false

    override suspend fun getInstalledPackageInfo(packageName: String): SystemPackageInfo? = null

    override suspend fun getAllInstalledPackageNames(): Set<String> = emptySet()

    override suspend fun getAllInstalledApps(): List<DeviceApp> = emptyList()
}

class IosInstallerInfoExtractor : InstallerInfoExtractor {
    override suspend fun extractPackageInfo(filePath: String): ApkPackageInfo? = null
}

/** Shizuku is Android-only. */
class IosInstallerStatusProvider : InstallerStatusProvider {
    override val shizukuAvailability: StateFlow<ShizukuAvailability> =
        MutableStateFlow(ShizukuAvailability.UNAVAILABLE).asStateFlow()

    override fun requestShizukuPermission() = Unit
}

/** No periodic background update checks on iOS. */
class IosUpdateScheduleManager : UpdateScheduleManager {
    override fun reschedule(intervalHours: Long) = Unit
}
