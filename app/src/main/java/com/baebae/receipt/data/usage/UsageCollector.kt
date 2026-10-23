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
    /** 이날 아침에 끝난 밤잠 추정치. 판단할 수 없으면 null. */
    val sleepMs: Long?,
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
        if (dayEnd <= dayStart) return DaySummary(date, emptyMap(), 0, 0, null)
        val lateNightEnd = dayStart + LATE_NIGHT_HOURS * HOUR
        val queryStart = dayStart - LOOKBACK_MS

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

        val sleep = SleepEstimator(dayStart, now)
        var screenOffSince: Long? = null
        var sawScreenEvent = false

        val events = usm.queryEvents(queryStart, dayEnd)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val t = event.timeStamp
            when (event.eventType) {
                ACTIVITY_RESUMED -> tracker.resumed(event.packageName, event.className.orEmpty(), t)
                ACTIVITY_PAUSED, ACTIVITY_STOPPED -> tracker.paused(event.packageName, event.className.orEmpty(), t)
                SCREEN_NON_INTERACTIVE, DEVICE_SHUTDOWN -> {
                    tracker.closeAll(t)
                    if (screenOffSince == null) screenOffSince = t
                    sawScreenEvent = true
                }
                SCREEN_INTERACTIVE -> {
                    if (t in dayStart until dayEnd) screenOn++
                    // 조회 구간의 첫 화면 이벤트가 '켜짐'이면 그 전까지는 꺼져 있었던 것이다.
                    val offStart = screenOffSince ?: if (!sawScreenEvent) queryStart else null
                    if (offStart != null) sleep.offInterval(offStart, t, ongoing = false)
                    screenOffSince = null
                    sawScreenEvent = true
                }
                KEYGUARD_HIDDEN -> if (t in dayStart until dayEnd) keyguardHidden++
            }
        }
        tracker.closeAll(dayEnd)
        screenOffSince?.let { sleep.offInterval(it, dayEnd, ongoing = true) }

        // 잠금 화면을 쓰지 않는 기기는 KEYGUARD_HIDDEN 이 없으므로 화면 켜짐 횟수로 대신한다.
        val unlocks = if (keyguardHidden > 0) keyguardHidden else screenOn
        return DaySummary(date, appMs, unlocks, lateNightMs, sleep.result())
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
        /** 이 길이 이상 화면이 꺼져 있어야 수면으로 본다. */
        const val MIN_SLEEP_MS = 2 * HOUR
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

/**
 * 밤 시간대(전날 21:00 ~ 당일 12:00) 가운데 가장 긴 화면 꺼짐 구간을 수면으로 추정한다.
 * 아직 끝나지 않은 꺼짐 구간은 창이 닫히기 전이면 확정하지 않는다(지금 자는 중일 수 있다).
 */
internal class SleepEstimator(dayStart: Long, private val now: Long) {
    val windowStart = dayStart - 3 * UsageCollector.HOUR
    val windowEnd = dayStart + 12 * UsageCollector.HOUR
    private var longest = 0L

    fun offInterval(start: Long, end: Long, ongoing: Boolean) {
        if (ongoing && now < windowEnd) return
        val length = UsageCollector.overlap(start, end, windowStart, windowEnd)
        if (length > longest) longest = length
    }

    fun result(): Long? = longest.takeIf { it >= UsageCollector.MIN_SLEEP_MS }
}
