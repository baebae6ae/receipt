package com.baebae.receipt.data

import android.content.Context
import com.baebae.receipt.data.steps.StepRepository
import com.baebae.receipt.data.usage.DaySummary
import com.baebae.receipt.data.usage.InstalledApps
import com.baebae.receipt.data.usage.UsageCollector
import com.baebae.receipt.data.usage.UsagePermission
import com.baebae.receipt.domain.AppUsage
import com.baebae.receipt.domain.WeeklyStats
import com.baebae.receipt.util.WeekMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZonedDateTime

/** 주간 원자료를 모은다. 사용 기록 권한이 없으면 null. */
class StatsRepository(
    private val context: Context,
    private val collector: UsageCollector,
    private val installedApps: InstalledApps,
    private val steps: StepRepository,
) {

    fun hasUsagePermission(): Boolean = UsagePermission.isGranted(context)

    suspend fun weekStats(weekStart: LocalDate, now: ZonedDateTime = ZonedDateTime.now()): WeeklyStats? =
        withContext(Dispatchers.IO) {
            if (!hasUsagePermission()) return@withContext null
            val labels = installedApps.labelMap()
            val nowMillis = now.toInstant().toEpochMilli()
            val lastDay = minOf(weekStart.plusDays(6), now.toLocalDate())
            val days = generateSequence(weekStart) { it.plusDays(1) }
                .takeWhile { !it.isAfter(lastDay) }
                .map { collector.summarizeDay(it, labels.keys, nowMillis, now.zone) }
                .toList()
            val weekStartMillis = WeekMath.startOfDayMillis(weekStart, now.zone)
            val weekEndMillis = minOf(WeekMath.startOfDayMillis(weekStart.plusDays(7), now.zone), nowMillis)
            merge(weekStart, days, labels, steps.stepsBetween(weekStartMillis, weekEndMillis))
        }

    companion object {
        fun merge(weekStart: LocalDate, days: List<DaySummary>, labels: Map<String, String>, steps: Long): WeeklyStats {
            val appTotals = HashMap<String, Long>()
            days.forEach { day -> day.appMs.forEach { (pkg, ms) -> appTotals[pkg] = (appTotals[pkg] ?: 0L) + ms } }
            val apps = appTotals.entries
                .map { (pkg, ms) -> AppUsage(pkg, labels[pkg] ?: pkg, ms) }
                .sortedByDescending { it.foregroundMs }
            return WeeklyStats(
                weekStart = weekStart,
                days = days.size,
                apps = apps,
                screenMs = apps.sumOf { it.foregroundMs },
                unlocks = days.sumOf { it.unlocks },
                steps = steps,
                sleepTotalMs = days.sumOf { it.sleepMs ?: 0L },
                sleepNights = days.count { it.sleepMs != null },
                lateNightMs = days.sumOf { it.lateNightMs },
            )
        }
    }
}
