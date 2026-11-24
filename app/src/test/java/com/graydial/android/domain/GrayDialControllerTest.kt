package com.graydial.android.domain

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.graydial.android.data.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GrayDialControllerTest {

    @get:Rule val tmp = TemporaryFolder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repo by lazy {
        PreferencesRepository(
            PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("c.preferences_pb").also { it.delete() } },
        )
    }
    private val fake = FakeSaturationController()
    private var boot: Int? = 1
    private val controller by lazy { GrayDialController(repo, fake, bootCount = { boot }, clock = { 42L }) }

    @After fun tearDown() = scope.cancel()

    @Test
    fun `enabling applies the preferred intensity`() = runTest {
        repo.setIntensity(75)
        val outcome = controller.setEnabled(true)
        assertEquals(ApplyOutcome.Applied(25, Verification.CONFIRMED), outcome)
        assertEquals(listOf(25), fake.calls)
        assertEquals(25, repo.current().lastAppliedSaturation)
        assertEquals(1, repo.current().lastAppliedBootCount)
    }

    @Test
    fun `disabling restores normal saturation and keeps intensity`() = runTest {
        repo.setIntensity(60)
        controller.setEnabled(true)
        controller.setEnabled(false)
        assertEquals(listOf(40, 100), fake.calls)
        assertEquals(60, repo.current().intensity)
        assertEquals(100, repo.current().lastAppliedSaturation)
    }

    @Test
    fun `intensity changes while disabled are stored but not applied`() = runTest {
        assertNull(controller.setIntensity(30))
        assertTrue(fake.calls.isEmpty())
        assertEquals(30, repo.current().intensity)
    }

    @Test
    fun `intensity changes while enabled are applied`() = runTest {
        controller.setEnabled(true)
        controller.setIntensity(90)
        assertEquals(listOf(50, 10), fake.calls)
    }

    @Test
    fun `unchanged value does not repeat the privileged call`() = runTest {
        controller.setEnabled(true)
        val second = controller.setIntensity(50)
        assertEquals(ApplyOutcome.Unchanged(50), second)
        assertEquals(1, fake.calls.size)
    }

    @Test
    fun `failure rolls back the master switch and keeps last applied state`() = runTest {
        controller.setEnabled(true) // applied 50
        controller.setEnabled(false) // applied 100
        fake.failWith = SaturationException.ShizukuNotRunning()
        val outcome = controller.setEnabled(true)
        assertTrue(outcome is ApplyOutcome.Failed)
        assertFalse(repo.current().enabled)
        assertEquals(100, repo.current().lastAppliedSaturation)
        assertEquals(outcome, controller.lastOutcome.value)
    }

    @Test
    fun `failed slider change keeps preference but not applied state`() = runTest {
        controller.setEnabled(true)
        fake.failWith = SaturationException.Timeout()
        val outcome = controller.setIntensity(80)
        assertTrue(outcome is ApplyOutcome.Failed)
        assertEquals(80, repo.current().intensity)
        assertEquals(50, repo.current().lastAppliedSaturation)
    }

    @Test
    fun `restore always sends normal saturation even if believed applied`() = runTest {
        controller.restoreOriginalColors()
        controller.restoreOriginalColors()
        assertEquals(listOf(100, 100), fake.calls)
    }

    @Test
    fun `restore disables and preserves intensity`() = runTest {
        repo.setIntensity(65)
        controller.setEnabled(true)
        controller.restoreOriginalColors()
        assertFalse(repo.current().enabled)
        assertEquals(65, repo.current().intensity)
    }

    @Test
    fun `preset enables and applies`() = runTest {
        val outcome = controller.selectPreset(Preset.MONOCHROME)
        assertEquals(0, outcome.saturation)
        assertTrue(repo.current().enabled)
        assertEquals(100, repo.current().intensity)
    }

    @Test
    fun `original preset restores colors`() = runTest {
        controller.selectPreset(Preset.FOCUS)
        controller.selectPreset(Preset.ORIGINAL)
        assertEquals(listOf(25, 100), fake.calls)
        assertFalse(repo.current().enabled)
        assertEquals(0, repo.current().intensity)
    }

    @Test
    fun `toggle uses last applied state, not requested state`() = runTest {
        repo.setEnabled(true) // requested but never applied
        controller.toggle()
        assertEquals(listOf(50), fake.calls) // turned on, because nothing was active
        controller.toggle()
        assertEquals(listOf(50, 100), fake.calls)
    }

    @Test
    fun `reboot clears stale applied state without reapplying`() = runTest {
        controller.setEnabled(true)
        boot = 2
        controller.reconcileWithBoot()
        val p = repo.current()
        assertNull(p.lastAppliedSaturation)
        assertFalse(p.enabled)
        assertTrue(p.resetByRestart)
        assertEquals(1, fake.calls.size)
    }

    @Test
    fun `same boot keeps applied state`() = runTest {
        controller.setEnabled(true)
        controller.reconcileWithBoot()
        assertEquals(50, repo.current().lastAppliedSaturation)
        assertTrue(repo.current().enabled)
    }

    @Test
    fun `restart notice Reapply (setEnabled true) reapplies grayscale`() = runTest {
        repo.setIntensity(70)
        controller.setEnabled(true)
        boot = 2
        controller.reconcileWithBoot()
        controller.setEnabled(true) // exactly what the notice's Reapply button calls
        assertEquals(listOf(30, 30), fake.calls)
        assertTrue(repo.current().enabled)
        assertFalse(repo.current().resetByRestart)
    }

    @Test
    fun `reapply re-sends the requested state even if unchanged`() = runTest {
        controller.setEnabled(true)
        controller.reapply()
        assertEquals(listOf(50, 50), fake.calls)
    }

    @Test
    fun `verification mismatch is reported`() = runTest {
        fake.activated = Result.success(false)
        val outcome = controller.setEnabled(true)
        assertEquals(Verification.MISMATCH, (outcome as ApplyOutcome.Applied).verification)
    }

    @Test
    fun `verification unavailable when query fails`() = runTest {
        fake.activated = Result.failure(SaturationException.Unsupported("x"))
        val outcome = controller.setEnabled(true)
        assertEquals(Verification.UNAVAILABLE, (outcome as ApplyOutcome.Applied).verification)
    }

    @Test
    fun `recovery after failure succeeds`() = runTest {
        fake.failWith = SaturationException.ServiceUnavailable("gone")
        controller.setEnabled(true)
        fake.failWith = null
        val outcome = controller.setEnabled(true)
        assertTrue(outcome is ApplyOutcome.Applied)
        assertTrue(repo.current().enabled)
    }
}
