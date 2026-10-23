package com.baebae.receipt.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** 주 단위 계산. 한 주는 월요일 00:00 ~ 일요일 24:00 (기기 시간대). */
object WeekMath {

    /** 매주 일요일 이 시각에 그 주 카드를 발행한다. */
    val PUBLISH_DAY: DayOfWeek = DayOfWeek.SUNDAY
    val PUBLISH_TIME: LocalTime = LocalTime.of(20, 0)

    fun weekStartOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun currentWeekStart(zone: ZoneId = ZoneId.systemDefault()): LocalDate = weekStartOf(LocalDate.now(zone))

    fun startOfDayMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    /** 지금 이후 가장 가까운 발행 시각. */
    fun nextPublishAt(now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val thisWeek = now.toLocalDate()
            .with(TemporalAdjusters.nextOrSame(PUBLISH_DAY))
            .atTime(PUBLISH_TIME)
            .atZone(now.zone)
        return if (thisWeek.isAfter(now)) thisWeek else thisWeek.plusWeeks(1)
    }

    fun toLocalDateTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), zone)
}
