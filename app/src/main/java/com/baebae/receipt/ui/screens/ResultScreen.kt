package com.baebae.receipt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baebae.receipt.ui.card.CardFormat
import com.baebae.receipt.ui.card.CardTemplate
import com.baebae.receipt.ui.card.LabelCardView
import com.baebae.receipt.ui.card.ScaledCard
import com.baebae.receipt.ui.card.rememberSampleCard

// 결과 카드 화면에는 광고를 넣지 않는다(배너 포함).
@Composable
fun ResultScreen() {
    val card = rememberSampleCard()
    var format by rememberSaveable { mutableStateOf(CardFormat.STORY) }
    var template by rememberSaveable { mutableStateOf(CardTemplate.CLASSIC) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScaledCard(format = format, modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)) {
            LabelCardView(card, template, format, showWatermark = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardFormat.entries.forEach { option ->
                FilterChip(
                    selected = format == option,
                    onClick = { format = option },
                    label = { Text(stringResource(option.label)) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardTemplate.entries.forEach { option ->
                FilterChip(
                    selected = template == option,
                    onClick = { template = option },
                    label = { Text(stringResource(option.label)) },
                )
            }
        }
    }
}
