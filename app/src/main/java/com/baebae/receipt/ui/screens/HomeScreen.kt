package com.baebae.receipt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.ui.card.CardFormat
import com.baebae.receipt.ui.card.CardTemplate
import com.baebae.receipt.ui.card.LabelCardView
import com.baebae.receipt.ui.card.ScaledCard
import com.baebae.receipt.ui.card.rememberSampleCard

@Composable
fun HomeScreen(onOpenResult: () -> Unit) {
    val card = rememberSampleCard()
    ScreenFrame(title = stringResource(R.string.home_title)) {
        ScaledCard(
            format = CardFormat.SQUARE,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenResult),
        ) {
            LabelCardView(card, CardTemplate.CLASSIC, CardFormat.SQUARE, showWatermark = true)
        }
    }
}
