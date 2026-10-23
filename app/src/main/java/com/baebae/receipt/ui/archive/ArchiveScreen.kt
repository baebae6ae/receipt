package com.baebae.receipt.ui.archive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.data.StoredCard
import com.baebae.receipt.ui.card.CardFormat
import com.baebae.receipt.ui.card.CardTemplate
import com.baebae.receipt.ui.card.CardText
import com.baebae.receipt.ui.card.LabelCardView
import com.baebae.receipt.ui.card.ScaledCard
import com.baebae.receipt.ui.theme.Mono

@Composable
fun ArchiveScreen(onOpen: (StoredCard) -> Unit) {
    val cards = appContainer().cards
    val flow = remember { cards.observeArchive() }
    val archive by flow.collectAsStateWithLifecycle(initialValue = null)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(text = stringResource(R.string.archive_title), style = MaterialTheme.typography.headlineMedium)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
        val list = archive
        if (list != null && list.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.archive_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(list.orEmpty(), key = { it.weekStart.toEpochDay() }) { stored ->
            ArchiveItem(stored, onClick = { onOpen(stored) })
        }
    }
}

@Composable
private fun ArchiveItem(stored: StoredCard, onClick: () -> Unit) {
    val cards = appContainer().cards
    val card = remember(stored) { cards.toLabel(stored) }
    val template = CardTemplate.entries.firstOrNull { it.name == stored.template } ?: CardTemplate.CLASSIC
    Column(modifier = Modifier.clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ScaledCard(format = CardFormat.SQUARE, modifier = Modifier.fillMaxWidth()) {
            LabelCardView(card, template, CardFormat.SQUARE, showWatermark = false)
        }
        Text(text = CardText.lot(stored.weekStart), style = TextStyle(fontFamily = Mono, fontSize = 11.sp))
        Text(
            text = CardText.dateRange(card.periodStart, card.periodEnd),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
