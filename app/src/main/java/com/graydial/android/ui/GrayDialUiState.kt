package com.graydial.android.ui

import com.graydial.android.domain.ApplyOutcome
import com.graydial.android.domain.GrayscaleIntensity
import com.graydial.android.domain.PermissionState
import com.graydial.android.domain.Preset
import com.graydial.android.domain.SaturationController
import com.graydial.android.domain.Verification

data class GrayDialUiState(
    val loaded: Boolean = false,
    /** Requested: master switch. */
    val enabled: Boolean = false,
    /** Requested: preferred grayscale intensity in percent. */
    val intensity: Int = 50,
    /** Last saturation the system accepted, or null if unknown (e.g. after a restart). */
    val appliedSaturation: Int? = null,
    val appliedAtMillis: Long? = null,
    val resetByRestart: Boolean = false,
    val permission: PermissionState = PermissionState.SHIZUKU_NOT_RUNNING,
    val working: Boolean = false,
    val lastOutcome: ApplyOutcome? = null,
    val customPresets: List<Int> = emptyList(),
) {
    /** The current intensity can be saved as a new preset. */
    val canSaveCurrentAsPreset: Boolean
        get() = Preset.matching(intensity) == null && intensity !in customPresets &&
            customPresets.size < com.graydial.android.data.GrayDialPreferences.MAX_CUSTOM_PRESETS

    val selectedPreset: Preset? get() = Preset.matching(intensity)

    val appliedIntensity: Int? get() = appliedSaturation?.let { GrayscaleIntensity.fromSaturation(it).percent }

    /** True when what the user asked for differs from what the system last accepted. */
    val pendingChange: Boolean
        get() {
            val requested = if (enabled) GrayscaleIntensity.of(intensity).saturation else SaturationController.NORMAL_SATURATION
            // Unknown applied state with normal colors requested is the normal boot state, not pending.
            if (appliedSaturation == null) return requested != SaturationController.NORMAL_SATURATION
            return appliedSaturation != requested
        }
}

/** One-line description of the actual system state, never overstating what is known. */
fun GrayDialUiState.appliedDescription(): String {
    val applied = appliedIntensity
    val verification = (lastOutcome as? ApplyOutcome.Applied)
        ?.takeIf { it.saturation == appliedSaturation }
        ?.verification
    val base = when {
        applied == null && resetByRestart -> "Colors were reset by a device restart."
        applied == null -> "No change applied by GrayDial since the last restart."
        applied == 0 -> "Normal colors applied."
        else -> "$applied% grayscale applied."
    }
    val suffix = when (verification) {
        Verification.CONFIRMED -> " The system confirms the filter is ${if ((applied ?: 0) > 0) "on" else "off"}."
        Verification.MISMATCH -> " Warning: the system reports a different filter state. Another app or setting may have overridden it."
        Verification.UNAVAILABLE -> " Command succeeded; the system state could not be read back."
        null -> ""
    }
    return base + suffix
}
