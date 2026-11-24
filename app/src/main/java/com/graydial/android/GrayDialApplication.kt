package com.graydial.android

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.datastore.preferences.preferencesDataStore
import com.graydial.android.data.PreferencesRepository
import com.graydial.android.domain.GrayDialController
import com.graydial.android.system.ShizukuConnectionManager
import com.graydial.android.system.ShizukuSaturationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.dataStore by preferencesDataStore(name = "graydial")

/** Manual dependency container; the app is small enough not to need a DI framework. */
class AppContainer(app: Application) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val preferences = PreferencesRepository(app.dataStore)
    val shizuku = ShizukuConnectionManager(app)
    val saturation = ShizukuSaturationController(shizuku)
    val controller = GrayDialController(
        preferences = preferences,
        saturation = saturation,
        bootCount = {
            runCatching { Settings.Global.getInt(app.contentResolver, Settings.Global.BOOT_COUNT) }.getOrNull()
        },
    )
}

class GrayDialApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.shizuku.start()
    }
}

val Context.appContainer: AppContainer get() = (applicationContext as GrayDialApplication).container
