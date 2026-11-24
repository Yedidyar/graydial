package com.graydial.android.ui

import com.graydial.android.domain.ApplyOutcome
import com.graydial.android.domain.Preset
import com.graydial.android.domain.Verification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrayDialUiStateTest {

    @Test
    fun `unverified command success is not described as verified`() {
        val s = GrayDialUiState(enabled = true, intensity = 75, appliedSaturation = 25,
            lastOutcome = ApplyOutcome.Applied(25, Verification.UNAVAILABLE))
        assertEquals("75% grayscale applied. Command succeeded; the system state could not be read back.", s.appliedDescription())
    }

    @Test
    fun `confirmed state`() {
        val s = GrayDialUiState(enabled = true, intensity = 75, appliedSaturation = 25,
            lastOutcome = ApplyOutcome.Applied(25, Verification.CONFIRMED))
        assertTrue(s.appliedDescription().contains("confirms the filter is on"))
    }

    @Test
    fun `unknown state after restart`() {
        assertEquals("Colors were reset by a device restart.", GrayDialUiState(resetByRestart = true).appliedDescription())
        assertEquals("No change applied by GrayDial since the last restart.", GrayDialUiState().appliedDescription())
    }

    @Test
    fun `pending change detection`() {
        assertTrue(GrayDialUiState(enabled = true, intensity = 40, appliedSaturation = 100).pendingChange)
        assertFalse(GrayDialUiState(enabled = true, intensity = 40, appliedSaturation = 60).pendingChange)
        assertFalse(GrayDialUiState(enabled = false, intensity = 40, appliedSaturation = 100).pendingChange)
        // Fresh install / after restart: nothing applied, nothing requested.
        assertFalse(GrayDialUiState(enabled = false, intensity = 50, appliedSaturation = null).pendingChange)
        assertTrue(GrayDialUiState(enabled = true, intensity = 50, appliedSaturation = null).pendingChange)
    }

    @Test
    fun `preset selection reflects intensity`() {
        assertEquals(Preset.BALANCED, GrayDialUiState(intensity = 50).selectedPreset)
        assertEquals(null, GrayDialUiState(intensity = 51).selectedPreset)
    }
}
