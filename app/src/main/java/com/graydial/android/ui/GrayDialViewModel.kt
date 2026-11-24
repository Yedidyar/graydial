package com.graydial.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.graydial.android.appContainer
import com.graydial.android.domain.LatestValueApplier
import com.graydial.android.domain.Preset
import com.graydial.android.tile.GrayDialTileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GrayDialViewModel(app: Application) : AndroidViewModel(app) {

    private val container = app.appContainer
    private val controller = container.controller

    /** Slider position while dragging, shown immediately before persistence catches up. */
    private val sliderOverride = MutableStateFlow<Int?>(null)
    private val inFlight = MutableStateFlow(0)

    private val sliderApplier = LatestValueApplier<Int>(viewModelScope, SLIDER_DEBOUNCE_MS) { percent ->
        track { controller.setIntensity(percent) }
    }

    val uiState: StateFlow<GrayDialUiState> = combine(
        container.preferences.preferences,
        container.shizuku.state,
        sliderOverride,
        inFlight,
        controller.lastOutcome,
    ) { prefs, permission, override, running, outcome ->
        GrayDialUiState(
            loaded = true,
            enabled = prefs.enabled,
            intensity = override ?: prefs.intensity,
            appliedSaturation = prefs.lastAppliedSaturation,
            appliedAtMillis = prefs.lastAppliedAtMillis,
            resetByRestart = prefs.resetByRestart,
            permission = permission,
            working = running > 0,
            lastOutcome = outcome,
            customPresets = prefs.customPresets,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GrayDialUiState())

    init {
        viewModelScope.launch { controller.reconcileWithBoot() }
    }

    fun onResume() = container.shizuku.refresh()

    fun onIntensityChanged(percent: Int) {
        sliderOverride.value = percent.coerceIn(0, 100)
        sliderApplier.submit(percent.coerceIn(0, 100))
    }

    fun onEnabledChanged(enabled: Boolean) = launchTracked { controller.setEnabled(enabled) }

    fun onPresetSelected(preset: Preset) {
        sliderOverride.value = preset.percent
        launchTracked { controller.selectPreset(preset) }
    }

    fun onCustomPresetSelected(percent: Int) {
        sliderOverride.value = percent
        launchTracked { controller.applyIntensity(percent) }
    }

    fun onSaveCurrentAsPreset() {
        val percent = uiState.value.intensity
        viewModelScope.launch { container.preferences.addCustomPreset(percent) }
    }

    fun onRemoveCustomPreset(percent: Int) {
        viewModelScope.launch { container.preferences.removeCustomPreset(percent) }
    }

    fun onRestoreOriginalColors() = launchTracked { controller.restoreOriginalColors() }

    fun onReapply() = launchTracked { controller.reapply() }

    fun onDismissRestartNotice() {
        viewModelScope.launch { controller.dismissRestartNotice() }
    }

    fun onRequestPermission(): Boolean = container.shizuku.requestPermission()

    fun isShizukuInstalled(): Boolean = container.shizuku.isShizukuInstalled()

    private fun launchTracked(block: suspend () -> Unit) {
        viewModelScope.launch { track(block) }
    }

    private suspend fun track(block: suspend () -> Unit) {
        inFlight.update { it + 1 }
        try {
            block()
        } finally {
            inFlight.update { it - 1 }
            GrayDialTileService.requestRefresh(getApplication())
        }
    }

    private companion object {
        /** Short enough to feel live while dragging, long enough to avoid flooding the system. */
        const val SLIDER_DEBOUNCE_MS = 120L
    }
}
