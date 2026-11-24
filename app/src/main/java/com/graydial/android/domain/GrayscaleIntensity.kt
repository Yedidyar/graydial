package com.graydial.android.domain

/**
 * User-facing grayscale intensity in percent: 0 = full color, 100 = monochrome.
 *
 * The system API works in the opposite direction ("saturation", 100 = normal), so all
 * conversion goes through this type to keep the inverse relationship in one place.
 */
@JvmInline
value class GrayscaleIntensity private constructor(val percent: Int) {

    /** System saturation level expected by ColorDisplayService (0..100). */
    val saturation: Int get() = MAX - percent

    companion object {
        const val MIN = 0
        const val MAX = 100

        val ORIGINAL = GrayscaleIntensity(MIN)

        /** Strict constructor: rejects out-of-range values. */
        fun of(percent: Int): GrayscaleIntensity {
            require(percent in MIN..MAX) { "Grayscale intensity must be in $MIN..$MAX, was $percent" }
            return GrayscaleIntensity(percent)
        }

        fun ofOrNull(percent: Int): GrayscaleIntensity? =
            if (percent in MIN..MAX) GrayscaleIntensity(percent) else null

        /** Lenient constructor for UI input (e.g. slider float values). */
        fun coerce(percent: Int): GrayscaleIntensity = GrayscaleIntensity(percent.coerceIn(MIN, MAX))

        fun fromSaturation(saturation: Int): GrayscaleIntensity {
            require(isValidSaturation(saturation)) { "Saturation must be in 0..100, was $saturation" }
            return GrayscaleIntensity(MAX - saturation)
        }

        fun isValidSaturation(saturation: Int): Boolean = saturation in 0..100
    }
}

/** Built-in presets. Any intermediate integer value is also allowed. */
enum class Preset(val label: String, val percent: Int) {
    ORIGINAL("Original", 0),
    SUBTLE("Subtle", 25),
    BALANCED("Balanced", 50),
    FOCUS("Focus", 75),
    MONOCHROME("Monochrome", 100);

    val intensity: GrayscaleIntensity get() = GrayscaleIntensity.of(percent)

    companion object {
        fun matching(percent: Int): Preset? = entries.firstOrNull { it.percent == percent }
    }
}
