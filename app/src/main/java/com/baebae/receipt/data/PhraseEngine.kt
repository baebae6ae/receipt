package com.baebae.receipt.data

import android.content.Context
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.domain.PhrasePicker
import com.baebae.receipt.domain.WeeklyStats
import com.baebae.receipt.domain.phrase.PhraseBank
import com.baebae.receipt.domain.phrase.PhraseCategory
import com.baebae.receipt.domain.phrase.PhraseMetrics
import com.baebae.receipt.domain.phrase.PhraseSelector
import java.util.Locale
import kotlin.random.Random

/**
 * 조건별 문구 은행에서 고른다. AI API 를 부르지 않고 assets 의 JSON 만 쓴다.
 * 최근 사용한 문구 id 는 [AppPrefs]에 남겨 같은 문구가 연달아 나오지 않게 한다.
 */
class PhraseEngine(
    private val context: Context,
    private val prefs: AppPrefs,
) : PhrasePicker {

    private val banks = HashMap<String, PhraseBank>()

    @Synchronized
    private fun bank(): PhraseBank {
        val lang = if (Locale.getDefault().language == "ko") "ko" else "en"
        return banks.getOrPut(lang) {
            val json = context.assets.open("phrases_$lang.json").bufferedReader().use { it.readText() }
            PhraseBank.parse(json)
        }
    }

    override fun preview(stats: WeeklyStats, seed: Long): ChosenPhrases = choose(stats, Random(seed), record = false)

    override fun pickForPublish(stats: WeeklyStats): ChosenPhrases = choose(stats, Random.Default, record = true)

    override fun reroll(stats: WeeklyStats, current: ChosenPhrases, category: String, record: Boolean): ChosenPhrases {
        val bank = bank()
        val metrics = PhraseMetrics.from(stats)
        val currentIds = current.cautions.map { it.id } + listOfNotNull(current.storage?.id, current.expiry?.id)
        val picked = PhraseSelector.select(bank, category, metrics, prefs.recentPhrases(category), Random.Default, exclude = currentIds.toSet())
        if (picked.isEmpty()) return current
        if (record) prefs.addRecentPhrases(category, picked.map { it.id })
        return when (category) {
            PhraseCategory.CAUTION -> current.copy(cautions = picked)
            PhraseCategory.STORAGE -> current.copy(storage = picked.first())
            PhraseCategory.EXPIRY -> current.copy(expiry = picked.first())
            else -> current
        }
    }

    override fun localize(phrases: ChosenPhrases): ChosenPhrases {
        val bank = bank()
        fun Phrase.local() = copy(text = bank.text(id) ?: text)
        return ChosenPhrases(phrases.cautions.map { it.local() }, phrases.storage?.local(), phrases.expiry?.local())
    }

    override fun topAppFallback(): String = bank().fallbackTopApp

    private fun choose(stats: WeeklyStats, random: Random, record: Boolean): ChosenPhrases {
        val bank = bank()
        val metrics = PhraseMetrics.from(stats)
        fun pick(category: String): List<Phrase> =
            PhraseSelector.select(bank, category, metrics, prefs.recentPhrases(category), random).also { chosen ->
                if (record) prefs.addRecentPhrases(category, chosen.map { it.id })
            }
        return ChosenPhrases(
            cautions = pick(PhraseCategory.CAUTION),
            storage = pick(PhraseCategory.STORAGE).firstOrNull(),
            expiry = pick(PhraseCategory.EXPIRY).firstOrNull(),
        )
    }
}
