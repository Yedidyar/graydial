package com.graydial.android.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LatestValueApplierTest {

    @Test
    fun `rapid input applies only the final value`() = runTest {
        val applied = mutableListOf<Int>()
        val applier = LatestValueApplier<Int>(backgroundScope, 120) { applied += it }
        runCurrent()
        for (v in 0..60 step 5) {
            applier.submit(v)
            advanceTimeBy(30)
        }
        advanceTimeBy(500)
        assertEquals(listOf(60), applied)
    }

    @Test
    fun `values separated by more than the debounce window are all applied`() = runTest {
        val applied = mutableListOf<Int>()
        val applier = LatestValueApplier<Int>(backgroundScope, 120) { applied += it }
        runCurrent()
        applier.submit(10)
        advanceTimeBy(200)
        applier.submit(20)
        advanceTimeBy(200)
        assertEquals(listOf(10, 20), applied)
    }
}
