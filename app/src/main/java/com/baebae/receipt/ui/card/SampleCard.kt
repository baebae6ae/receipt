package com.baebae.receipt.ui.card

import com.baebae.receipt.domain.AppUsage
import com.baebae.receipt.domain.WeeklyStats
import com.baebae.receipt.util.WeekMath
import java.time.LocalDate

/** 권한 전 홈 미리보기와 디자인 확인용 예시 원자료. */
object SampleCard {
    private const val HOUR = 3_600_000L

    fun stats(today: LocalDate = LocalDate.now()): WeeklyStats {
        val screenMs = 32 * HOUR + 15 * 60_000L
        return WeeklyStats(
            weekStart = WeekMath.weekStartOf(today),
            days = 7,
            apps = listOf(
                AppUsage("com.google.android.youtube", "YouTube", 14 * HOUR),
                AppUsage("com.instagram.android", "Instagram", 6 * HOUR),
                AppUsage("com.kakao.talk", "KakaoTalk", 4 * HOUR + 20 * 60_000L),
                AppUsage("com.android.chrome", "Chrome", 2 * HOUR),
            ),
            screenMs = screenMs,
            unlocks = 612,
            steps = 41_203,
            sleepTotalMs = 7 * (6 * HOUR + 12 * 60_000L),
            sleepNights = 7,
            lateNightMs = (screenMs * 0.24).toLong(),
        )
    }
}
