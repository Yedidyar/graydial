package com.graydial.android

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rikka.shizuku.Shizuku

/**
 * Real device integration: requires Shizuku running and GrayDial already authorized.
 * Skipped (not failed) when those preconditions are missing. Always restores normal colors.
 */
@RunWith(AndroidJUnit4::class)
class ShizukuIntegrationTest {

    private val container get() =
        InstrumentationRegistry.getInstrumentation().targetContext.appContainer

    @Before
    fun requireShizuku() {
        Thread.sleep(1500) // Allow Shizuku to deliver its binder to the freshly started process.
        assumeTrue("Shizuku not running", Shizuku.pingBinder())
        assumeTrue("GrayDial not authorized in Shizuku", Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
    }

    @After
    fun restore() = runBlocking {
        container.saturation.restoreDefaults()
        Unit
    }

    @Test
    fun appliesAndRestoresSaturation() = runBlocking {
        container.saturation.setSaturation(50).let { assertTrue("set 50: ${it.exceptionOrNull()}", it.isSuccess) }
        val active = container.saturation.isSaturationActivated()
        assertTrue("read back: ${active.exceptionOrNull()}", active.getOrThrow())

        container.saturation.setSaturation(100).let { assertTrue("set 100: ${it.exceptionOrNull()}", it.isSuccess) }
        val inactive = container.saturation.isSaturationActivated()
        assertFalse("read back: ${inactive.exceptionOrNull()}", inactive.getOrThrow())
    }

    @Test
    fun appLayerRejectsInvalidValuesWithoutCallingService() = runBlocking {
        assertTrue(container.saturation.setSaturation(101).isFailure)
        assertTrue(container.saturation.setSaturation(-1).isFailure)
    }

    @Test
    fun everyPresetLevelIsAcceptedAndReadBack() = runBlocking {
        for (saturation in listOf(100, 75, 50, 25, 0, 100)) {
            container.saturation.setSaturation(saturation).let { assertTrue("set $saturation: ${it.exceptionOrNull()}", it.isSuccess) }
            val active = container.saturation.isSaturationActivated()
            assertTrue("read back at $saturation: ${active.exceptionOrNull()}", active.isSuccess)
            assertEquals("activated at $saturation", saturation < 100, active.getOrThrow())
        }
    }

    @Test
    fun repeatedRapidCallsAllSucceed() = runBlocking {
        repeat(40) { i ->
            val level = if (i % 2 == 0) 60 else 100
            container.saturation.setSaturation(level).let { assertTrue("call $i: ${it.exceptionOrNull()}", it.isSuccess) }
        }
    }
}
