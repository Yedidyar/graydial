package com.graydial.android.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.graydial.android.domain.GrayscaleIntensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Everything GrayDial persists. Stored only in app-private storage, excluded from backups.
 *
 * [enabled] and [intensity] are what the user *requested*. [lastAppliedSaturation] is the last
 * value the system accepted. They are deliberately separate.
 */
data class GrayDialPreferences(
    val enabled: Boolean = false,
    val intensity: Int = DEFAULT_INTENSITY,
    val lastAppliedSaturation: Int? = null,
    val lastAppliedBootCount: Int? = null,
    val lastAppliedAtMillis: Long? = null,
    /** True when a restart cleared an active transform and the user has not acted since. */
    val resetByRestart: Boolean = false,
) {
    companion object {
        const val DEFAULT_INTENSITY = 50
    }
}

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val preferences: Flow<GrayDialPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map(::read)

    suspend fun current(): GrayDialPreferences = preferences.first()

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit {
            it[ENABLED] = enabled
            it[RESET_BY_RESTART] = false
        }
    }

    suspend fun setIntensity(percent: Int) {
        val valid = GrayscaleIntensity.of(percent) // rejects invalid input
        dataStore.edit { it[INTENSITY] = valid.percent }
    }

    suspend fun recordApplied(saturation: Int, bootCount: Int?, atMillis: Long) {
        require(GrayscaleIntensity.isValidSaturation(saturation)) { "Invalid saturation $saturation" }
        dataStore.edit {
            it[LAST_APPLIED] = saturation
            if (bootCount != null) it[LAST_APPLIED_BOOT] = bootCount else it.remove(LAST_APPLIED_BOOT)
            it[LAST_APPLIED_AT] = atMillis
            it[RESET_BY_RESTART] = false
        }
    }

    /** The device restarted, so the system transform no longer reflects [lastAppliedSaturation]. */
    suspend fun markResetByRestart(wasActive: Boolean) {
        dataStore.edit {
            it.remove(LAST_APPLIED)
            it.remove(LAST_APPLIED_BOOT)
            it.remove(LAST_APPLIED_AT)
            it[ENABLED] = false
            it[RESET_BY_RESTART] = wasActive
        }
    }

    suspend fun dismissRestartNotice() {
        dataStore.edit { it[RESET_BY_RESTART] = false }
    }

    private fun read(p: Preferences): GrayDialPreferences = GrayDialPreferences(
        enabled = p[ENABLED] ?: false,
        // Corrupt or out-of-range stored values fall back to safe defaults.
        intensity = p[INTENSITY]?.takeIf { it in GrayscaleIntensity.MIN..GrayscaleIntensity.MAX }
            ?: GrayDialPreferences.DEFAULT_INTENSITY,
        lastAppliedSaturation = p[LAST_APPLIED]?.takeIf(GrayscaleIntensity::isValidSaturation),
        lastAppliedBootCount = p[LAST_APPLIED_BOOT],
        lastAppliedAtMillis = p[LAST_APPLIED_AT],
        resetByRestart = p[RESET_BY_RESTART] ?: false,
    )

    private companion object {
        val ENABLED = booleanPreferencesKey("enabled")
        val INTENSITY = intPreferencesKey("intensity")
        val LAST_APPLIED = intPreferencesKey("last_applied_saturation")
        val LAST_APPLIED_BOOT = intPreferencesKey("last_applied_boot_count")
        val LAST_APPLIED_AT = longPreferencesKey("last_applied_at")
        val RESET_BY_RESTART = booleanPreferencesKey("reset_by_restart")
    }
}
