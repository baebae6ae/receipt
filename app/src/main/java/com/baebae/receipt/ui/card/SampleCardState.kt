package com.baebae.receipt.ui.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.domain.Phrase

@Composable
fun rememberSampleCard(): LabelCard = SampleCard.create(
    productName = stringResource(R.string.default_nickname),
    cautions = listOf(stringResource(R.string.sample_caution_1), stringResource(R.string.sample_caution_2)),
    storage = stringResource(R.string.sample_storage),
    expiry = stringResource(R.string.sample_expiry),
)

/** 문구 은행 연결 전까지 쓰는 고정 문구. */
@Composable
fun rememberSamplePhrases(): ChosenPhrases = ChosenPhrases(
    cautions = listOf(
        Phrase("sample.caution.0", stringResource(R.string.sample_caution_1)),
        Phrase("sample.caution.1", stringResource(R.string.sample_caution_2)),
    ),
    storage = Phrase("sample.storage", stringResource(R.string.sample_storage)),
    expiry = Phrase("sample.expiry", stringResource(R.string.sample_expiry)),
)
