package com.baebae.receipt.data

import android.content.Context
import com.baebae.receipt.data.db.DailyStatsDao
import com.baebae.receipt.data.db.DailyStatsEntity
import com.baebae.receipt.data.steps.StepRepository
import com.baebae.receipt.data.usage.InstalledApps
import com.baebae.receipt.data.usage.UsageCollector
import com.baebae.receipt.data.usage.UsagePermission
import com.baebae.receipt.domain.AppUsage
import com.baebae.receipt.domain.WeeklyStats
import com.baebae.receipt.util.WeekMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** 하루치 기록. 캐시(Room)와 새로 계산한 값을 같은 모양으로 다룬다. */
data class DayRecord(
    val date: LocalDate,
    val apps: List<AppUsage>,
    val unlocks: Int,
    val lateNightMs: Long,
    val sleepMs: Long?,
)

/**
 * 주간 원자료를 모은다. 지난 날짜는 [DailyStatsDao] 캐시를 우선 쓰고,
 * 아직 확정되지 않은 날(오늘·어제)만 UsageEvents 에서 새로 계산해 캐시를 갱신한다.
 */
class StatsRepository(
    private val context: Context,
    private val collector: UsageCollector,
    private val installedApps: InstalledApps,
    private val steps: StepRepository,
    private val dailyDao: DailyStatsDao,
) {

    fun hasUsagePermission(): Boolean = UsagePermission.isGranted(context)

    /** 사용 기록 권한이 없으면 null. */
    suspend fun weekStats(weekStart: LocalDate, now: ZonedDateTime = ZonedDateTime.now()): WeeklyStats? =
        withContext(Dispatchers.IO) {
            if (!hasUsagePermission()) return@withContext null
            val labels = installedApps.labelMap()
            val lastDay = minOf(weekStart.plusDays(6), now.toLocalDate())
            if (lastDay.isBefore(weekStart)) return@withContext WeeklyStats.empty(weekStart)
            val records = days(weekStart, lastDay, labels, now)
            val nowMillis = now.toInstant().toEpochMilli()
            val weekStartMillis = WeekMath.startOfDayMillis(weekStart, now.zone)
            val weekEndMillis = minOf(WeekMath.startOfDayMillis(weekStart.plusDays(7), now.zone), nowMillis)
            merge(weekStart, records, labels, steps.stepsBetween(weekStartMillis, weekEndMillis))
        }

    /** 워커에서 호출: 최근 이틀을 다시 계산해 캐시에 넣고 오래된 캐시를 지운다. */
    suspend fun refreshRecentDays(now: ZonedDateTime = ZonedDateTime.now()) = withContext(Dispatchers.IO) {
        if (!hasUsagePermission()) return@withContext
        val labels = installedApps.labelMap()
        val today = now.toLocalDate()
        days(today.minusDays(1), today, labels, now, forceFresh = true)
        dailyDao.deleteOlderThan(today.minusDays(CACHE_DAYS).toEpochDay())
    }

    private suspend fun days(
        from: LocalDate,
        to: LocalDate,
        labels: Map<String, String>,
        now: ZonedDateTime,
        forceFresh: Boolean = false,
    ): List<DayRecord> {
        val nowMillis = now.toInstant().toEpochMilli()
        val cached = dailyDao.between(from.toEpochDay(), to.toEpochDay()).associateBy { it.epochDay }
        val dates = generateSequence(from) { it.plusDays(1) }.takeWhile { !it.isAfter(to) }.toList()
        return dates.map { date ->
                val entry = cached[date.toEpochDay()]
                if (!forceFresh && entry != null && isFinal(entry, date, now.zone)) {
                    entry.toRecord()
                } else {
                    val summary = collector.summarizeDay(date, labels.keys, nowMillis, now.zone)
                    val entity = DailyStatsEntity(
                        epochDay = date.toEpochDay(),
                        appsJson = CardJson.daySummaryApps(summary, labels),
                        unlocks = summary.unlocks,
                        lateNightMs = summary.lateNightMs,
                        sleepMs = summary.sleepMs,
                        updatedAt = nowMillis,
                    )
                    dailyDao.upsert(entity)
                    entity.toRecord()
                }
            }
    }

    /** 그날이 끝난 뒤에 계산된 값이면 더 바뀌지 않는다. */
    private fun isFinal(entry: DailyStatsEntity, date: LocalDate, zone: ZoneId): Boolean =
        entry.updatedAt >= WeekMath.startOfDayMillis(date.plusDays(1), zone)

    private fun DailyStatsEntity.toRecord() = DayRecord(
        date = LocalDate.ofEpochDay(epochDay),
        apps = CardJson.appsFromJson(appsJson),
        unlocks = unlocks,
        lateNightMs = lateNightMs,
        sleepMs = sleepMs,
    )

    companion object {
        private const val CACHE_DAYS = 120L

        /** 앱 이름은 지금 설치된 이름을 우선하고, 지워진 앱은 저장해 둔 이름을 쓴다. */
        fun merge(weekStart: LocalDate, days: List<DayRecord>, labels: Map<String, String>, steps: Long): WeeklyStats {
            val totals = LinkedHashMap<String, Long>()
            val savedLabels = HashMap<String, String>()
            days.forEach { day ->
                day.apps.forEach { app ->
                    totals[app.packageName] = (totals[app.packageName] ?: 0L) + app.foregroundMs
                    savedLabels[app.packageName] = app.label
                }
            }
            val apps = totals.entries
                .map { (pkg, ms) -> AppUsage(pkg, labels[pkg] ?: savedLabels[pkg] ?: pkg, ms) }
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
