package com.baebae.receipt.data

import com.baebae.receipt.data.usage.SleepEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SleepEstimatorTest {
    private val h = 3_600_000L
    private val day = 100 * 24 * h // 임의의 자정

    @Test
    fun picksLongestOffIntervalInsideNightWindow() {
        val e = SleepEstimator(day, now = day + 20 * h)
        e.offInterval(day - 2 * h, day + 7 * h, ongoing = false) // 22시~07시 = 9시간
        e.offInterval(day + 8 * h, day + 9 * h, ongoing = false)
        assertEquals(9 * h, e.result())
    }

    @Test
    fun clipsToWindow() {
        val e = SleepEstimator(day, now = day + 20 * h)
        e.offInterval(day - 6 * h, day + 2 * h, ongoing = false) // 18시~02시 → 21시부터만
        assertEquals(5 * h, e.result())
    }

    @Test
    fun shortOrOngoingIsNotSleep() {
        val short = SleepEstimator(day, now = day + 20 * h)
        short.offInterval(day + h, day + 2 * h, ongoing = false)
        assertNull(short.result())

        val ongoing = SleepEstimator(day, now = day + 4 * h)
        ongoing.offInterval(day - h, day + 4 * h, ongoing = true)
        assertNull(ongoing.result())
    }
}
