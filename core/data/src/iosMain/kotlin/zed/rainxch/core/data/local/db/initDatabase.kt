package zed.rainxch.core.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import zed.rainxch.core.data.utils.iosDocumentsDir

fun initDatabase(): AppDatabase =
    Room
        .databaseBuilder<AppDatabase>(
            name = "${iosDocumentsDir()}/github_store.db",
        ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
