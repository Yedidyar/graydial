package com.graydial.android.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.graydial.android.domain.GrayscaleIntensity
import com.graydial.android.domain.Preset
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
    /** User-added presets (percent), sorted, without duplicates of the built-in presets. */
    val customPresets: List<Int> = emptyList(),
) {
    companion object {
        const val DEFAULT_INTENSITY = 50
        const val MAX_CUSTOM_PRESETS = 8
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

    /** Adds [percent] as a custom preset. Returns false if it already exists or the list is full. */
    suspend fun addCustomPreset(percent: Int): Boolean {
        val valid = GrayscaleIntensity.of(percent).percent
        var added = false
        dataStore.edit {
            val current = decodePresets(it[CUSTOM_PRESETS])
            if (Preset.matching(valid) == null && valid !in current &&
                current.size < GrayDialPreferences.MAX_CUSTOM_PRESETS
            ) {
                it[CUSTOM_PRESETS] = encodePresets(current + valid)
                added = true
            }
        }
        return added
    }

    suspend fun removeCustomPreset(percent: Int) {
        dataStore.edit {
            it[CUSTOM_PRESETS] = encodePresets(decodePresets(it[CUSTOM_PRESETS]) - percent)
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
        customPresets = decodePresets(p[CUSTOM_PRESETS]),
    )

    /** Stored as "95,85". Invalid, duplicate or built-in values are dropped on read. */
    private fun decodePresets(raw: String?): List<Int> = raw.orEmpty()
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it in GrayscaleIntensity.MIN..GrayscaleIntensity.MAX && Preset.matching(it) == null }
        .distinct()
        .sorted()
        .take(GrayDialPreferences.MAX_CUSTOM_PRESETS)

    private fun encodePresets(values: List<Int>): String = values.distinct().sorted().joinToString(",")

    private companion object {
        val ENABLED = booleanPreferencesKey("enabled")
        val INTENSITY = intPreferencesKey("intensity")
        val LAST_APPLIED = intPreferencesKey("last_applied_saturation")
        val LAST_APPLIED_BOOT = intPreferencesKey("last_applied_boot_count")
        val LAST_APPLIED_AT = longPreferencesKey("last_applied_at")
        val RESET_BY_RESTART = booleanPreferencesKey("reset_by_restart")
        val CUSTOM_PRESETS = stringPreferencesKey("custom_presets")
    }
}
