package com.graydial.android.domain

import com.graydial.android.data.GrayDialPreferences
import com.graydial.android.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** How confident we are that the display matches what was requested. */
enum class Verification {
    /** The system reported the transform's active/inactive state and it matches. */
    CONFIRMED,
    /** The system reported a state that contradicts the request. */
    MISMATCH,
    /** The system could not be queried; only command success is known. */
    UNAVAILABLE,
}

sealed interface ApplyOutcome {
    val saturation: Int

    data class Applied(override val saturation: Int, val verification: Verification) : ApplyOutcome
    /** Nothing sent: the same value was already applied successfully in this boot. */
    data class Unchanged(override val saturation: Int) : ApplyOutcome
    data class Failed(override val saturation: Int, val error: Throwable) : ApplyOutcome
}

/**
 * The single place that turns user intent into privileged operations. Shared by the UI and the
 * Quick Settings tile so both always agree on persisted state.
 *
 * Invariants:
 *  - [GrayDialPreferences.lastAppliedSaturation] only changes after the system accepted a value.
 *  - A failed on/off change rolls the master switch back, so it never claims an unapplied state.
 *  - Operations are serialized; the latest request always wins.
 */
class GrayDialController(
    private val preferences: PreferencesRepository,
    private val saturation: SaturationController,
    private val bootCount: () -> Int?,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()
    private val _lastOutcome = MutableStateFlow<ApplyOutcome?>(null)
    val lastOutcome: StateFlow<ApplyOutcome?> = _lastOutcome.asStateFlow()

    /**
     * Global saturation lives only in system memory and is reset by a reboot. If the last
     * applied value was recorded in an earlier boot, forget it and turn the switch off.
     * Nothing is reapplied automatically; the user is told and can reapply with one tap.
     */
    suspend fun reconcileWithBoot() = mutex.withLock {
        val p = preferences.current()
        val boot = bootCount() ?: return@withLock
        val recordedBoot = p.lastAppliedBootCount ?: return@withLock
        if (p.lastAppliedSaturation != null && recordedBoot != boot) {
            preferences.markResetByRestart(wasActive = p.lastAppliedSaturation < SaturationController.NORMAL_SATURATION)
        }
    }

    suspend fun setEnabled(enabled: Boolean): ApplyOutcome = mutex.withLock {
        val before = preferences.current()
        preferences.setEnabled(enabled)
        val outcome = applyLocked(preferences.current(), force = false)
        if (outcome is ApplyOutcome.Failed) preferences.setEnabled(before.enabled)
        outcome
    }

    /** Stores the preferred intensity; applies it only when the master switch is on. */
    suspend fun setIntensity(percent: Int): ApplyOutcome? = mutex.withLock {
        preferences.setIntensity(GrayscaleIntensity.of(percent).percent)
        val p = preferences.current()
        if (p.enabled) applyLocked(p, force = false) else null
    }

    /** Presets also switch grayscale on, except "Original", which restores normal colours. */
    suspend fun selectPreset(preset: Preset): ApplyOutcome {
        if (preset == Preset.ORIGINAL) return restoreOriginalColors(keepIntensity = false)
        return mutex.withLock {
            val before = preferences.current()
            preferences.setIntensity(preset.percent)
            preferences.setEnabled(true)
            val outcome = applyLocked(preferences.current(), force = false)
            if (outcome is ApplyOutcome.Failed) preferences.setEnabled(before.enabled)
            outcome
        }
    }

    /**
     * The recovery path: always sends "saturation 100", even if we believe it is already applied,
     * because the system may have been changed by something else.
     */
    suspend fun restoreOriginalColors(keepIntensity: Boolean = true): ApplyOutcome = mutex.withLock {
        val before = preferences.current()
        if (!keepIntensity) preferences.setIntensity(Preset.ORIGINAL.percent)
        preferences.setEnabled(false)
        val outcome = applyLocked(preferences.current(), force = true)
        if (outcome is ApplyOutcome.Failed) preferences.setEnabled(before.enabled)
        outcome
    }

    /** Quick Settings toggle, based on the last *successfully applied* state. */
    suspend fun toggle(): ApplyOutcome {
        val p = preferences.current()
        return setEnabled(!isActive(p))
    }

    /** Re-sends the requested state, e.g. after a restart or after reconnecting Shizuku. */
    suspend fun reapply(): ApplyOutcome = mutex.withLock {
        applyLocked(preferences.current(), force = true)
    }

    suspend fun dismissRestartNotice() = preferences.dismissRestartNotice()

    private suspend fun applyLocked(p: GrayDialPreferences, force: Boolean): ApplyOutcome {
        val target = targetSaturation(p)
        if (!force && p.lastAppliedSaturation == target) {
            return ApplyOutcome.Unchanged(target).also { _lastOutcome.value = it }
        }
        val result = saturation.setSaturation(target)
        val outcome = result.fold(
            onSuccess = {
                preferences.recordApplied(target, bootCount(), clock())
                ApplyOutcome.Applied(target, verify(target))
            },
            onFailure = { ApplyOutcome.Failed(target, it) },
        )
        _lastOutcome.value = outcome
        return outcome
    }

    private suspend fun verify(target: Int): Verification {
        val expectedActive = target < SaturationController.NORMAL_SATURATION
        return saturation.isSaturationActivated().fold(
            onSuccess = { active -> if (active == expectedActive) Verification.CONFIRMED else Verification.MISMATCH },
            onFailure = { Verification.UNAVAILABLE },
        )
    }

    companion object {
        fun targetSaturation(p: GrayDialPreferences): Int =
            if (p.enabled) GrayscaleIntensity.of(p.intensity).saturation else SaturationController.NORMAL_SATURATION

        /** Whether grayscale was last successfully applied (and not since reset by a restart). */
        fun isActive(p: GrayDialPreferences): Boolean =
            (p.lastAppliedSaturation ?: SaturationController.NORMAL_SATURATION) < SaturationController.NORMAL_SATURATION
    }
}
