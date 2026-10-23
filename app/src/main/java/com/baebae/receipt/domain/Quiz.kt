package com.baebae.receipt.domain

import java.time.LocalDate

/**
 * 권한 없이 해보는 질문형 성분표. 답을 가상의 한 주 원자료로 바꿔 [LabelBuilder]에 그대로 넣는다.
 * 질문/보기 문구는 strings.xml 에 있고 여기서는 값만 정한다.
 */
object Quiz {

    const val QUESTION_COUNT = 5

    enum class AppCategory(val packageKey: String) {
        VIDEO("quiz.video"), SNS("quiz.sns"), MESSENGER("quiz.messenger"), GAME("quiz.game"), WEBTOON("quiz.webtoon"),
    }

    /** 각 질문의 보기 개수. 화면은 이 개수만큼 strings 의 보기를 그린다. */
    val optionCounts = intArrayOf(4, AppCategory.entries.size, 3, 3, 3)

    private val dailyScreenHours = doubleArrayOf(1.5, 3.0, 5.0, 7.5)
    private val lateNightRatio = doubleArrayOf(0.03, 0.12, 0.32)
    private val sleepHours = doubleArrayOf(7.4, 6.4, 5.1)
    private val dailySteps = longArrayOf(2_400, 6_500, 11_800)
    private val dailyUnlocks = intArrayOf(28, 85, 160)

    /**
     * @param answers 질문 순서대로 고른 보기 인덱스
     * @param categoryLabel 앱 분류 → 표시 이름 (현지화는 호출하는 쪽에서)
     */
    fun toStats(answers: List<Int>, weekStart: LocalDate, categoryLabel: (AppCategory) -> String): WeeklyStats {
        require(answers.size == QUESTION_COUNT)
        val days = 7
        val hour = 3_600_000.0
        val screenMs = (dailyScreenHours[answers[0]] * hour * days).toLong()
        val top = AppCategory.entries[answers[1]]
        val others = AppCategory.entries.filter { it != top }
        val shares = listOf(0.46, 0.22, 0.13)
        val apps = (listOf(top) + others.take(2)).mapIndexed { i, category ->
            AppUsage(category.packageKey, categoryLabel(category), (screenMs * shares[i]).toLong())
        }
        val sleepMs = (sleepHours[answers[2]] * hour).toLong()
        return WeeklyStats(
            weekStart = weekStart,
            days = days,
            apps = apps,
            screenMs = screenMs,
            unlocks = dailyUnlocks[answers[4]] * days,
            steps = dailySteps[answers[3]] * days,
            sleepTotalMs = sleepMs * days,
            sleepNights = days,
            lateNightMs = (screenMs * lateNightRatio[answers[2]]).toLong(),
        )
    }
}
