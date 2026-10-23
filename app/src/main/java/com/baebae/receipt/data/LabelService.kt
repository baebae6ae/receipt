package com.baebae.receipt.data

import android.content.Context
import com.baebae.receipt.R
import com.baebae.receipt.data.steps.StepRepository
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.LabelBuilder
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.domain.WeeklyStats
import com.baebae.receipt.util.WeekMath

/** 원자료 + 설정(닉네임) + 문구 → 카드. */
class LabelService(
    private val context: Context,
    private val prefs: AppPrefs,
    private val stats: StatsRepository,
    private val steps: StepRepository,
) {

    fun productName(): String = prefs.nickname.ifEmpty { context.getString(R.string.default_nickname) }

    /** 진행 중인 이번 주 카드. 권한이 없으면 null. */
    suspend fun currentWeekCard(): LabelCard? {
        // 앱이 열려 있을 때는 센서 값을 확실히 받을 수 있으니 이때도 스냅샷을 남긴다.
        steps.snapshotIfStale()
        val weekStats = stats.weekStats(WeekMath.currentWeekStart()) ?: return null
        return build(weekStats)
    }

    fun build(weekStats: WeeklyStats): LabelCard =
        LabelBuilder.build(weekStats, productName(), samplePhrases())

    private fun samplePhrases() = ChosenPhrases(
        cautions = listOf(
            Phrase("sample.caution.0", context.getString(R.string.sample_caution_1)),
            Phrase("sample.caution.1", context.getString(R.string.sample_caution_2)),
        ),
        storage = Phrase("sample.storage", context.getString(R.string.sample_storage)),
        expiry = Phrase("sample.expiry", context.getString(R.string.sample_expiry)),
    )
}
