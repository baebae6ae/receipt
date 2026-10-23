package com.baebae.receipt.domain

import com.baebae.receipt.util.WeekMath
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class WeekMathTest {
    private val seoul = ZoneId.of("Asia/Seoul")
    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int = 0) = ZonedDateTime.of(y, m, d, h, min, 0, 0, seoul)

    @Test
    fun lastDueWeekSwitchesAtSundayEightPm() {
        // 2026-10-19 (월) ~ 10-25 (일)
        assertEquals(LocalDate.of(2026, 10, 12), WeekMath.lastDueWeek(at(2026, 10, 25, 19, 59)))
        assertEquals(LocalDate.of(2026, 10, 19), WeekMath.lastDueWeek(at(2026, 10, 25, 20, 0)))
        // 늦게 돈 작업(월요일 아침)도 지난주를 발행한다.
        assertEquals(LocalDate.of(2026, 10, 19), WeekMath.lastDueWeek(at(2026, 10, 26, 8, 0)))
    }

    @Test
    fun nextPublishIsUpcomingSundayEvening() {
        assertEquals(at(2026, 10, 25, 20), WeekMath.nextPublishAt(at(2026, 10, 21, 10)))
        assertEquals(at(2026, 11, 1, 20), WeekMath.nextPublishAt(at(2026, 10, 25, 20)))
    }
}
