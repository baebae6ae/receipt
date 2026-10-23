package com.baebae.receipt.ui.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.domain.LabelCard

@Composable
fun rememberSampleCard(): LabelCard = SampleCard.create(
    productName = stringResource(R.string.sample_product_name),
    cautions = listOf(stringResource(R.string.sample_caution_1), stringResource(R.string.sample_caution_2)),
    storage = stringResource(R.string.sample_storage),
    expiry = stringResource(R.string.sample_expiry),
)
