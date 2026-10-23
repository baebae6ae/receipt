package com.baebae.receipt.domain

import java.time.LocalDate

data class AppUsage(val packageName: String, val label: String, val foregroundMs: Long)

/** 한 주(월~일) 동안 모은 원자료. 카드 문구는 여기서 계산한 지표로 고른다. */
data class WeeklyStats(
    val weekStart: LocalDate,
    /** 실제로 집계된 일수(1..7). 발행 시점이나 권한 허용 시점에 따라 7보다 작을 수 있다. */
    val days: Int,
    /** 사용 시간 내림차순. */
    val apps: List<AppUsage>,
    val screenMs: Long,
    val unlocks: Int,
    val steps: Long,
    val sleepTotalMs: Long,
    val sleepNights: Int,
    /** 00:00~05:00 사이 화면 사용 시간. */
    val lateNightMs: Long,
) {
    val avgSleepMs: Long? get() = if (sleepNights > 0) sleepTotalMs / sleepNights else null
    val weekEnd: LocalDate get() = weekStart.plusDays(6)

    companion object {
        fun empty(weekStart: LocalDate) = WeeklyStats(weekStart, 0, emptyList(), 0, 0, 0, 0, 0, 0)
    }
}
