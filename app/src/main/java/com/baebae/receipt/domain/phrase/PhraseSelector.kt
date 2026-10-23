package com.baebae.receipt.domain.phrase

import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.domain.WeeklyStats
import kotlin.random.Random

/** 문구 조건에 쓰는 지표. 측정되지 않은 값은 NaN 으로 두어 어떤 조건에도 걸리지 않게 한다. */
object PhraseMetrics {
    const val LATE_NIGHT_RATIO = "lateNightRatio"
    const val DAILY_SCREEN_HOURS = "dailyScreenHours"
    const val DAILY_STEPS = "dailySteps"
    const val AVG_SLEEP_HOURS = "avgSleepHours"
    const val DAILY_UNLOCKS = "dailyUnlocks"
    const val TOP_APP_SHARE = "topAppShare"

    private const val HOUR = 3_600_000.0

    fun from(stats: WeeklyStats): Map<String, Double> {
        val days = stats.days.toDouble()
        val hasScreen = days > 0 && stats.screenMs > 0
        return mapOf(
            LATE_NIGHT_RATIO to if (hasScreen) stats.lateNightMs.toDouble() / stats.screenMs else Double.NaN,
            DAILY_SCREEN_HOURS to if (hasScreen) stats.screenMs / days / HOUR else Double.NaN,
            // 걸음 0 은 대개 권한·센서가 없다는 뜻이라 '안 걸음'으로 보지 않는다.
            DAILY_STEPS to if (days > 0 && stats.steps > 0) stats.steps / days else Double.NaN,
            AVG_SLEEP_HOURS to (stats.avgSleepMs?.let { it / HOUR } ?: Double.NaN),
            DAILY_UNLOCKS to if (days > 0 && stats.unlocks > 0) stats.unlocks / days else Double.NaN,
            TOP_APP_SHARE to if (hasScreen && stats.apps.isNotEmpty()) stats.apps[0].foregroundMs.toDouble() / stats.screenMs else Double.NaN,
        )
    }
}

object PhraseSelector {

    /**
     * 조건이 맞는 그룹 중 우선순위가 높은 것부터 그룹당 하나씩, 카테고리의 pick 개수만큼 고른다.
     * 같은 우선순위끼리는 섞고, 조건 그룹이 모자라면 기본 그룹에서 채운다.
     * [recent]에 있는 문구는 다른 후보가 있으면 피하고, [exclude]는 아예 고르지 않는다.
     */
    fun select(
        bank: PhraseBank,
        category: String,
        metrics: Map<String, Double>,
        recent: Set<String>,
        random: Random,
        exclude: Set<String> = emptySet(),
    ): List<Phrase> {
        val def = bank.categories[category] ?: return emptyList()
        val matched = def.groups.filter { it.matches(metrics) }
        val specific = matched.filterNot { it.isDefault }
            .groupBy { it.priority }
            .toSortedMap(compareByDescending { it })
            .values
            .flatMap { it.shuffled(random) }
        val defaults = matched.filter { it.isDefault }

        val result = mutableListOf<Phrase>()
        for (group in specific + defaults) {
            if (result.size >= def.pick) break
            pickFrom(category, group, recent, exclude + result.map { it.id }, random)?.let { result += it }
        }
        // pick 이 2 이상인데 맞는 그룹이 하나뿐이면 기본 그룹에서 더 꺼낸다.
        var guard = 0
        while (result.size < def.pick && defaults.isNotEmpty() && guard++ < def.pick * 4) {
            val group = defaults[random.nextInt(defaults.size)]
            pickFrom(category, group, recent, exclude + result.map { it.id }, random)?.let { result += it }
        }
        return result
    }

    private fun pickFrom(
        category: String,
        group: PhraseGroup,
        recent: Set<String>,
        exclude: Set<String>,
        random: Random,
    ): Phrase? {
        val candidates = group.phrases.indices
            .map { PhraseBank.id(category, group.id, it) }
            .filterNot { it in exclude }
        if (candidates.isEmpty()) return null
        val fresh = candidates.filterNot { it in recent }
        val id = fresh.ifEmpty { candidates }.let { it[random.nextInt(it.size)] }
        return Phrase(id, group.phrases[id.substringAfterLast('.').toInt()])
    }
}
