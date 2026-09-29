package zed.rainxch.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module
import zed.rainxch.core.data.local.data_store.createDataStore
import zed.rainxch.core.data.local.db.AppDatabase
import zed.rainxch.core.data.local.db.initDatabase
import zed.rainxch.core.data.services.FileLocationsProvider
import zed.rainxch.core.data.services.IosDownloader
import zed.rainxch.core.data.services.IosFileLocationsProvider
import zed.rainxch.core.data.services.IosInstaller
import zed.rainxch.core.data.services.IosInstallerStatusProvider
import zed.rainxch.core.data.services.IosLocalizationManager
import zed.rainxch.core.data.services.IosPackageMonitor
import zed.rainxch.core.data.services.IosUpdateScheduleManager
import zed.rainxch.core.data.services.LocalizationManager
import zed.rainxch.core.data.utils.IosAppLauncher
import zed.rainxch.core.data.utils.IosBrowserHelper
import zed.rainxch.core.data.utils.IosClipboardHelper
import zed.rainxch.core.data.utils.IosShareManager
import zed.rainxch.core.domain.network.Downloader
import zed.rainxch.core.domain.system.Installer
import zed.rainxch.core.domain.system.InstallerStatusProvider
import zed.rainxch.core.domain.system.PackageMonitor
import zed.rainxch.core.domain.system.UpdateScheduleManager
import zed.rainxch.core.domain.utils.AppLauncher
import zed.rainxch.core.domain.utils.BrowserHelper
import zed.rainxch.core.domain.utils.ClipboardHelper
import zed.rainxch.core.domain.utils.ShareManager

actual val corePlatformModule =
    module {
        // Core
        single<Downloader> { IosDownloader(files = get()) }
        single<Installer> { IosInstaller() }
        single<FileLocationsProvider> { IosFileLocationsProvider() }
        single<PackageMonitor> { IosPackageMonitor() }
        single<LocalizationManager> { IosLocalizationManager() }

        // Locals
        single<AppDatabase> { initDatabase() }
        single<DataStore<Preferences>> { createDataStore() }

        // Utils
        single<BrowserHelper> { IosBrowserHelper() }
        single<ClipboardHelper> { IosClipboardHelper() }
        single<AppLauncher> { IosAppLauncher() }
        single<ShareManager> { IosShareManager() }
        single<InstallerStatusProvider> { IosInstallerStatusProvider() }
        single<UpdateScheduleManager> { IosUpdateScheduleManager() }
    }
