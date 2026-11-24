package com.graydial.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.graydial.android.system.ShizukuColorDisplay
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

/** Out-of-range values must be rejected before any binder call is made (no Shizuku needed). */
@RunWith(AndroidJUnit4::class)
class ColorDisplayValidationTest {
    @Test
    fun rejectsOutOfRangeValuesBeforeAnyCall() {
        val display = ShizukuColorDisplay()
        listOf(-1, 101, Int.MIN_VALUE, Int.MAX_VALUE, 1000).forEach {
            assertThrows(IllegalArgumentException::class.java) { display.setSaturation(it) }
        }
    }
}
