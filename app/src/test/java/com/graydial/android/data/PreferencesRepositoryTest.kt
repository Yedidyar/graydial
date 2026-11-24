package com.graydial.android.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

class PreferencesRepositoryTest {

    @get:Rule val tmp = TemporaryFolder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("test.preferences_pb").also { it.delete() } }
    }
    private val repo by lazy { PreferencesRepository(dataStore) }

    @After fun tearDown() = scope.cancel()

    @Test
    fun `defaults`() = runTest {
        val p = repo.current()
        assertFalse(p.enabled)
        assertEquals(50, p.intensity)
        assertNull(p.lastAppliedSaturation)
    }

    @Test
    fun `values persist`() = runTest {
        repo.setEnabled(true)
        repo.setIntensity(80)
        repo.recordApplied(20, bootCount = 7, atMillis = 1234)
        val p = repo.current()
        assertTrue(p.enabled)
        assertEquals(80, p.intensity)
        assertEquals(20, p.lastAppliedSaturation)
        assertEquals(7, p.lastAppliedBootCount)
        assertEquals(1234L, p.lastAppliedAtMillis)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid intensity is rejected`() = runTest {
        repo.setIntensity(101)
    }

    @Test
    fun `corrupt stored intensity falls back to default`() = runTest {
        dataStore.edit { it[intPreferencesKey("intensity")] = 999 }
        dataStore.edit { it[intPreferencesKey("last_applied_saturation")] = -4 }
        assertEquals(50, repo.current().intensity)
        assertNull(repo.current().lastAppliedSaturation)
    }

    @Test
    fun `restart reset clears applied state, disables, keeps intensity`() = runTest {
        repo.setEnabled(true)
        repo.setIntensity(70)
        repo.recordApplied(30, 1, 1)
        repo.markResetByRestart(wasActive = true)
        val p = repo.current()
        assertFalse(p.enabled)
        assertEquals(70, p.intensity)
        assertNull(p.lastAppliedSaturation)
        assertTrue(p.resetByRestart)
        repo.dismissRestartNotice()
        assertFalse(repo.current().resetByRestart)
    }
}
