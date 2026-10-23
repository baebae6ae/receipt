package com.baebae.receipt.data

import android.content.Context
import com.baebae.receipt.R
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.domain.PhrasePicker
import com.baebae.receipt.domain.WeeklyStats

/** 문구 은행 연결 전까지 쓰는 고정 문구. */
class SamplePhrasePicker(private val context: Context) : PhrasePicker {
    override fun preview(stats: WeeklyStats) = phrases()
    override fun pickForPublish(stats: WeeklyStats) = phrases()

    private fun phrases() = ChosenPhrases(
        cautions = listOf(
            Phrase("sample.caution.0", context.getString(R.string.sample_caution_1)),
            Phrase("sample.caution.1", context.getString(R.string.sample_caution_2)),
        ),
        storage = Phrase("sample.storage", context.getString(R.string.sample_storage)),
        expiry = Phrase("sample.expiry", context.getString(R.string.sample_expiry)),
    )
}
