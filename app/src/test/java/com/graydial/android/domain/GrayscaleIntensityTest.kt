package com.graydial.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrayscaleIntensityTest {

    @Test
    fun `intensity maps to inverse saturation`() {
        val table = mapOf(0 to 100, 25 to 75, 50 to 50, 75 to 25, 100 to 0)
        table.forEach { (intensity, saturation) ->
            assertEquals(saturation, GrayscaleIntensity.of(intensity).saturation)
        }
    }

    @Test
    fun `every valid value round trips`() {
        for (i in 0..100) {
            val intensity = GrayscaleIntensity.of(i)
            assertEquals(100 - i, intensity.saturation)
            assertEquals(intensity, GrayscaleIntensity.fromSaturation(intensity.saturation))
        }
    }

    @Test
    fun `boundaries are accepted`() {
        assertEquals(0, GrayscaleIntensity.of(0).percent)
        assertEquals(100, GrayscaleIntensity.of(100).percent)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative intensity is rejected`() {
        GrayscaleIntensity.of(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `intensity above 100 is rejected`() {
        GrayscaleIntensity.of(101)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid saturation is rejected`() {
        GrayscaleIntensity.fromSaturation(101)
    }

    @Test
    fun `lenient constructors`() {
        assertNull(GrayscaleIntensity.ofOrNull(Int.MAX_VALUE))
        assertNull(GrayscaleIntensity.ofOrNull(Int.MIN_VALUE))
        assertEquals(100, GrayscaleIntensity.coerce(250).percent)
        assertEquals(0, GrayscaleIntensity.coerce(-5).percent)
        assertFalse(GrayscaleIntensity.isValidSaturation(-1))
        assertTrue(GrayscaleIntensity.isValidSaturation(0))
    }

    @Test
    fun `presets have the specified values`() {
        assertEquals(
            listOf("Original" to 0, "Subtle" to 25, "Balanced" to 50, "Focus" to 75, "Monochrome" to 100),
            Preset.entries.map { it.label to it.percent },
        )
        assertEquals(Preset.FOCUS, Preset.matching(75))
        assertNull(Preset.matching(42))
        assertEquals(25, Preset.FOCUS.intensity.saturation)
    }
}
