package com.baebae.receipt.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.data.StoredCard
import com.baebae.receipt.domain.ItemKeys
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.domain.phrase.PhraseCategory
import com.baebae.receipt.ui.card.CardExportBar
import com.baebae.receipt.ui.card.CardFormat
import com.baebae.receipt.ui.card.CardTemplate
import com.baebae.receipt.ui.card.CardText
import com.baebae.receipt.ui.card.LabelCardView
import com.baebae.receipt.ui.card.ScaledCard
import com.baebae.receipt.ui.card.exportFileName
import com.baebae.receipt.ui.card.recordInto
import com.baebae.receipt.ui.card.rememberCardLayer
import com.baebae.receipt.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 진행 중인 이번 주를 뜻하는 week 인자 값. */
const val LIVE_WEEK = -1L

// 결과 카드 화면에는 광고를 넣지 않는다(배너 포함).
@Composable
fun ResultScreen(weekEpochDay: Long) {
    val cards = appContainer().cards
    var stored by remember { mutableStateOf<StoredCard?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(weekEpochDay) {
        stored = if (weekEpochDay == LIVE_WEEK) cards.liveCurrentWeek() else cards.get(LocalDate.ofEpochDay(weekEpochDay))
        stored?.let { if (it.published && !it.revealed) cards.markRevealed(it.weekStart) }
        loaded = true
    }

    val current = stored
    when {
        !loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        current == null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.result_unavailable), style = MaterialTheme.typography.bodyMedium)
        }
        else -> ResultContent(stored = current, onChange = { stored = it })
    }
}

@Composable
private fun ResultContent(stored: StoredCard, onChange: (StoredCard) -> Unit) {
    val cards = appContainer().cards
    val scope = rememberCoroutineScope()
    var format by rememberSaveable { mutableStateOf(CardFormat.STORY) }
    val template = remember(stored.template) {
        CardTemplate.entries.firstOrNull { it.name == stored.template } ?: CardTemplate.CLASSIC
    }
    val card = remember(stored) { cards.toLabel(stored) }
    val layer = rememberCardLayer()
    val rewards = rememberRewardState()

    // 발행된 카드만 저장한다. 진행 중인 주는 화면을 떠나면 원래대로 돌아간다.
    fun update(next: StoredCard) {
        onChange(next)
        if (!next.published) return
        scope.launch {
            if (next.hidden != stored.hidden) cards.setHidden(next.weekStart, next.hidden)
            if (next.template != stored.template) cards.setTemplate(next.weekStart, next.template)
            if (next.phrases != stored.phrases) cards.setPhrases(next.weekStart, next.phrases)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!stored.published) {
            Text(
                text = stringResource(R.string.result_live_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ScaledCard(
            format = format,
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp),
            contentModifier = Modifier.recordInto(layer),
        ) {
            LabelCardView(card, template, format, showWatermark = !rewards.removeWatermark)
        }
        CardExportBar(layer = layer, fileName = exportFileName(card, format), onExported = { rewards.onExported() })
        WatermarkToggle(rewards)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardFormat.entries.forEach { option ->
                FilterChip(selected = format == option, onClick = { format = option }, label = { Text(stringResource(option.label)) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardTemplate.entries.forEach { option ->
                val available = rewards.isAvailable(option)
                FilterChip(
                    selected = template == option,
                    onClick = {
                        if (available) update(stored.copy(template = option.name))
                        else rewards.askUnlock(option) { update(stored.copy(template = option.name)) }
                    },
                    label = {
                        Text(
                            if (available) stringResource(option.label)
                            else stringResource(R.string.template_locked, stringResource(option.label)),
                        )
                    },
                )
            }
        }

        RewardDialogs(rewards)

        SectionTitle(stringResource(R.string.result_edit_title))
        EditList(
            card = card,
            hidden = stored.hidden,
            onToggle = { key, visible ->
                update(stored.copy(hidden = if (visible) stored.hidden - key else stored.hidden + key))
            },
            onShowAll = { update(stored.copy(hidden = emptySet())) },
            onReroll = { category -> scope.launch { onChange(cards.reroll(stored, category)) } },
        )
    }
}

private class EditItem(val key: String, val label: String, val rerollCategory: String? = null)

@Composable
private fun EditList(
    card: LabelCard,
    hidden: Set<String>,
    onToggle: (key: String, visible: Boolean) -> Unit,
    onShowAll: () -> Unit,
    onReroll: (category: String) -> Unit,
) {
    val items = buildList {
        card.ingredients.forEach { add(EditItem(it.key, stringResource(R.string.result_edit_ingredient, CardText.ingredientName(it)))) }
        add(EditItem(ItemKeys.SCREEN, stringResource(R.string.nutrition_screen_time)))
        add(EditItem(ItemKeys.UNLOCKS, stringResource(R.string.nutrition_unlocks)))
        add(EditItem(ItemKeys.STEPS, stringResource(R.string.nutrition_steps)))
        add(EditItem(ItemKeys.SLEEP, stringResource(R.string.nutrition_sleep)))
        add(EditItem(ItemKeys.STORAGE, stringResource(R.string.card_row_storage), PhraseCategory.STORAGE))
        add(EditItem(ItemKeys.CAUTION, stringResource(R.string.card_row_caution), PhraseCategory.CAUTION))
        add(EditItem(ItemKeys.EXPIRY, stringResource(R.string.card_row_expiry), PhraseCategory.EXPIRY))
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            val visible = item.key !in hidden
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = visible, onCheckedChange = { onToggle(item.key, it) })
                Text(text = item.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                if (item.rerollCategory != null) {
                    TextButton(onClick = { onReroll(item.rerollCategory) }, enabled = visible) {
                        Text(stringResource(R.string.result_reroll))
                    }
                }
            }
        }
        TextButton(onClick = onShowAll, enabled = hidden.isNotEmpty()) {
            Text(stringResource(R.string.result_edit_show_all))
        }
    }
}
