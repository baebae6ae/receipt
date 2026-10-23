package com.baebae.receipt.ui.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.domain.LabelBuilder
import com.baebae.receipt.domain.LabelCard

@Composable
fun rememberSampleCard(): LabelCard {
    val phrases = appContainer().phrases
    val name = stringResource(R.string.default_nickname)
    return remember(name) {
        val stats = SampleCard.stats()
        LabelBuilder.build(stats, name, phrases.preview(stats), topAppFallback = phrases.topAppFallback())
    }
}
