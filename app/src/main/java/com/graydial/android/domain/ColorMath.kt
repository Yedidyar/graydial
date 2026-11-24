package com.graydial.android.domain

/**
 * Mirrors the matrix AOSP's GlobalSaturationTintController builds for a saturation level,
 * so the in-app preview shows the same mathematical result the system applies.
 *
 * For saturation s (0..1) and desaturation d = 1 - s, each output channel is
 *   out = s * in + d * (0.231 R + 0.715 G + 0.072 B)
 */
object ColorMath {
    const val LUMA_R = 0.231f
    const val LUMA_G = 0.715f
    const val LUMA_B = 0.072f

    /** Applies the system saturation transform to an sRGB colour with channels in 0..1. */
    fun desaturate(r: Float, g: Float, b: Float, saturation: Int): FloatArray {
        require(GrayscaleIntensity.isValidSaturation(saturation)) { "Saturation must be in 0..100" }
        val s = saturation / 100f
        val d = 1f - s
        val luma = d * (LUMA_R * r + LUMA_G * g + LUMA_B * b)
        return floatArrayOf(
            (s * r + luma).coerceIn(0f, 1f),
            (s * g + luma).coerceIn(0f, 1f),
            (s * b + luma).coerceIn(0f, 1f),
        )
    }

    /** Fully saturated hue (0..360) as sRGB channels in 0..1. */
    fun hue(degrees: Float): FloatArray {
        val h = ((degrees % 360f) + 360f) % 360f / 60f
        val x = 1f - kotlin.math.abs(h % 2f - 1f)
        return when (h.toInt()) {
            0 -> floatArrayOf(1f, x, 0f)
            1 -> floatArrayOf(x, 1f, 0f)
            2 -> floatArrayOf(0f, 1f, x)
            3 -> floatArrayOf(0f, x, 1f)
            4 -> floatArrayOf(x, 0f, 1f)
            else -> floatArrayOf(1f, 0f, x)
        }
    }
}
