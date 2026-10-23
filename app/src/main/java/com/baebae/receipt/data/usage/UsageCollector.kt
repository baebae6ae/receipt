package com.baebae.receipt.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.baebae.receipt.util.WeekMath
import java.time.LocalDate
import java.time.ZoneId

/** 하루치 화면 사용 요약. 모든 계산은 기기 안에서 끝난다. */
data class DaySummary(
    val date: LocalDate,
    /** 패키지 → 전경 시간(ms). 런처 앱만. */
    val appMs: Map<String, Long>,
    val unlocks: Int,
    /** 00:00~05:00 사이 전경 시간 합. */
    val lateNightMs: Long,
) {
    val screenMs: Long get() = appMs.values.sum()
}

/**
 * UsageEvents 를 직접 훑어 앱별 전경 시간과 잠금 해제 횟수를 계산한다.
 * queryUsageStats 의 일 단위 버킷은 경계가 기기마다 달라서 쓰지 않는다.
 */
class UsageCollector(context: Context) {

    private val usm = context.getSystemService(UsageStatsManager::class.java)

    fun summarizeDay(
        date: LocalDate,
        countedPackages: Set<String>,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): DaySummary {
        val dayStart = WeekMath.startOfDayMillis(date, zone)
        val dayEnd = minOf(WeekMath.startOfDayMillis(date.plusDays(1), zone), now)
        if (dayEnd <= dayStart) return DaySummary(date, emptyMap(), 0, 0)
        val lateNightEnd = dayStart + LATE_NIGHT_HOURS * HOUR

        val appMs = HashMap<String, Long>()
        var lateNightMs = 0L
        var keyguardHidden = 0
        var screenOn = 0

        // 자정을 걸친 세션을 잡기 위해 앞쪽을 여유 있게 조회하고 구간을 잘라 쓴다.
        val tracker = ForegroundTracker { pkg, start, end ->
            if (pkg !in countedPackages) return@ForegroundTracker
            val s = maxOf(start, dayStart)
            val e = minOf(end, dayEnd)
            if (e > s) {
                appMs[pkg] = (appMs[pkg] ?: 0L) + (e - s)
                lateNightMs += overlap(s, e, dayStart, lateNightEnd)
            }
        }

        val events = usm.queryEvents(dayStart - LOOKBACK_MS, dayEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val t = event.timeStamp
            when (event.eventType) {
                ACTIVITY_RESUMED -> tracker.resumed(event.packageName, event.className.orEmpty(), t)
                ACTIVITY_PAUSED, ACTIVITY_STOPPED -> tracker.paused(event.packageName, event.className.orEmpty(), t)
                SCREEN_NON_INTERACTIVE, DEVICE_SHUTDOWN -> tracker.closeAll(t)
                SCREEN_INTERACTIVE -> if (t in dayStart until dayEnd) screenOn++
                KEYGUARD_HIDDEN -> if (t in dayStart until dayEnd) keyguardHidden++
            }
        }
        tracker.closeAll(dayEnd)

        // 잠금 화면을 쓰지 않는 기기는 KEYGUARD_HIDDEN 이 없으므로 화면 켜짐 횟수로 대신한다.
        val unlocks = if (keyguardHidden > 0) keyguardHidden else screenOn
        return DaySummary(date, appMs, unlocks, lateNightMs)
    }

    /** 패키지별로 RESUMED 상태인 액티비티 집합을 들고, 집합이 비었다 차는 순간을 세션 경계로 본다. */
    private class ForegroundTracker(private val emit: (pkg: String, start: Long, end: Long) -> Unit) {
        private val active = HashMap<String, MutableSet<String>>()
        private val since = HashMap<String, Long>()

        fun resumed(pkg: String, cls: String, t: Long) {
            val set = active.getOrPut(pkg) { HashSet() }
            if (set.isEmpty()) since[pkg] = t
            set += cls
        }

        fun paused(pkg: String, cls: String, t: Long) {
            val set = active[pkg] ?: return
            if (!set.remove(cls)) return
            if (set.isEmpty()) since.remove(pkg)?.let { emit(pkg, it, t) }
        }

        fun closeAll(t: Long) {
            for ((pkg, start) in since) emit(pkg, start, t)
            since.clear()
            active.clear()
        }
    }

    companion object {
        const val HOUR = 3_600_000L
        const val LATE_NIGHT_HOURS = 5L
        private const val LOOKBACK_MS = 12 * HOUR

        // UsageEvents.Event 상수. 일부는 API 29 에서 이름이 붙었지만 값은 28 에서도 같다.
        private const val ACTIVITY_RESUMED = 1
        private const val ACTIVITY_PAUSED = 2
        private const val SCREEN_INTERACTIVE = 15
        private const val SCREEN_NON_INTERACTIVE = 16
        private const val KEYGUARD_HIDDEN = 18
        private const val ACTIVITY_STOPPED = 23
        private const val DEVICE_SHUTDOWN = 26

        fun overlap(s1: Long, e1: Long, s2: Long, e2: Long): Long = maxOf(0L, minOf(e1, e2) - maxOf(s1, s2))
    }
}
