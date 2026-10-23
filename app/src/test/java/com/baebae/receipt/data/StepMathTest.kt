package com.baebae.receipt.data

import com.baebae.receipt.data.steps.StepMath
import com.baebae.receipt.data.steps.StepSample
import org.junit.Assert.assertEquals
import org.junit.Test

class StepMathTest {
    private val h = 3_600_000L

    @Test
    fun splitsStepsProportionallyAcrossBoundary() {
        // 0시 1000보 → 10시 2000보: 0~5시에 절반
        val samples = listOf(StepSample(0, 1000, -h), StepSample(10 * h, 2000, -h))
        assertEquals(500, StepMath.stepsBetween(samples, 0, 5 * h))
        assertEquals(1000, StepMath.stepsBetween(samples, 0, 24 * h))
        assertEquals(0, StepMath.stepsBetween(samples, 10 * h, 20 * h))
    }

    @Test
    fun rebootCountsOnlyStepsSinceBoot() {
        // 4시에 재부팅. 재부팅 후 300보가 4~6시에 걸쳐 쌓였다.
        val samples = listOf(StepSample(0, 5000, -h), StepSample(6 * h, 300, 4 * h))
        assertEquals(300, StepMath.stepsBetween(samples, 0, 24 * h))
        assertEquals(150, StepMath.stepsBetween(samples, 0, 5 * h))
    }

    @Test
    fun emptyOrSingleSampleIsZero() {
        assertEquals(0, StepMath.stepsBetween(emptyList(), 0, h))
        assertEquals(0, StepMath.stepsBetween(listOf(StepSample(0, 10, 0)), 0, h))
    }
}
