package com.graydial.android.domain

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorMathTest {

    @Test
    fun `saturation 100 is identity`() {
        val rgb = floatArrayOf(0.9f, 0.2f, 0.4f)
        assertArrayEquals(rgb, ColorMath.desaturate(rgb[0], rgb[1], rgb[2], 100), 1e-6f)
    }

    @Test
    fun `saturation 0 produces equal channels using AOSP luminance weights`() {
        val out = ColorMath.desaturate(1f, 0f, 0f, 0)
        assertEquals(out[0], out[1], 1e-6f)
        assertEquals(out[1], out[2], 1e-6f)
        assertEquals(0.231f, out[0], 1e-6f)
    }

    @Test
    fun `half saturation matches the system matrix`() {
        // s = 0.5, d = 0.5; out_r = 0.5*r + 0.5*(0.231r + 0.715g + 0.072b)
        val out = ColorMath.desaturate(0f, 1f, 0f, 50)
        assertEquals(0.5f * 0.715f, out[0], 1e-6f)
        assertEquals(0.5f + 0.5f * 0.715f, out[1], 1e-6f)
        assertEquals(0.5f * 0.715f, out[2], 1e-6f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid saturation is rejected`() {
        ColorMath.desaturate(0f, 0f, 0f, 101)
    }

    @Test
    fun `hue wheel anchors`() {
        assertArrayEquals(floatArrayOf(1f, 0f, 0f), ColorMath.hue(0f), 1e-6f)
        assertArrayEquals(floatArrayOf(0f, 1f, 0f), ColorMath.hue(120f), 1e-6f)
        assertArrayEquals(floatArrayOf(0f, 0f, 1f), ColorMath.hue(240f), 1e-6f)
        assertArrayEquals(floatArrayOf(1f, 0f, 0f), ColorMath.hue(360f), 1e-6f)
    }
}
