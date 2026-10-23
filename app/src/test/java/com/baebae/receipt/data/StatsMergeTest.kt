package com.baebae.receipt.data

import com.baebae.receipt.domain.AppUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class StatsMergeTest {
    private val monday = LocalDate.of(2026, 10, 19)

    @Test
    fun sumsDaysAndPrefersCurrentLabels() {
        val days = listOf(
            DayRecord(monday, listOf(AppUsage("com.a", "Old A", 100), AppUsage("com.gone", "Gone", 50)), 10, 5, 7L),
            DayRecord(monday.plusDays(1), listOf(AppUsage("com.a", "Old A", 300)), 20, 0, null),
        )
        val stats = StatsRepository.merge(monday, days, labels = mapOf("com.a" to "A"), steps = 999)
        assertEquals(2, stats.days)
        assertEquals(listOf(AppUsage("com.a", "A", 400), AppUsage("com.gone", "Gone", 50)), stats.apps)
        assertEquals(450, stats.screenMs)
        assertEquals(30, stats.unlocks)
        assertEquals(7, stats.sleepTotalMs)
        assertEquals(1, stats.sleepNights)
        assertEquals(999, stats.steps)
    }

    @Test
    fun noSleepNightsMeansNullAverage() {
        val stats = StatsRepository.merge(monday, listOf(DayRecord(monday, emptyList(), 0, 0, null)), emptyMap(), 0)
        assertNull(stats.avgSleepMs)
    }
}
