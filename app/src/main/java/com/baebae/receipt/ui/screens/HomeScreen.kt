package com.baebae.receipt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.data.StoredCard
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.ui.card.CardFormat
import com.baebae.receipt.ui.card.CardTemplate
import com.baebae.receipt.ui.card.CardText
import com.baebae.receipt.ui.card.LabelCardView
import com.baebae.receipt.ui.card.ScaledCard
import com.baebae.receipt.ui.card.rememberSampleCard
import com.baebae.receipt.ui.components.RuledBox
import com.baebae.receipt.ui.components.SquareOutlinedButton
import com.baebae.receipt.util.WeekMath
import com.baebae.receipt.work.PublishWorker
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val REFRESH_INTERVAL_MS = 60_000L

@Composable
fun HomeScreen(onOpenResult: () -> Unit, onOpenPublished: (week: Long) -> Unit, onOpenPermissions: () -> Unit) {
    val container = appContainer()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(container.stats.hasUsagePermission()) }
    var liveCard by remember { mutableStateOf<LabelCard?>(null) }
    var unrevealed by remember { mutableStateOf<StoredCard?>(null) }
    val sample = rememberSampleCard()

    // 화면이 보이는 동안 1분마다 이번 주 카드를 다시 계산한다.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // 발행 작업이 밀렸으면 앱을 연 김에 발행한다(알림 없이, 아래 배너로 알린다).
            if (container.stats.hasUsagePermission()) PublishWorker.publishDue(context, notify = false)
            unrevealed = container.cards.latestUnrevealed()
            while (true) {
                hasPermission = container.stats.hasUsagePermission()
                liveCard = if (hasPermission) container.cards.liveCurrentWeek()?.let(container.cards::toLabel) else null
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    val publishFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.fmt_publish_datetime), Locale.getDefault())
    val nextPublish = remember { WeekMath.nextPublishAt() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.home_next_publish, nextPublish.format(publishFormatter)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        unrevealed?.let { card ->
            RuledBox {
                Text(text = stringResource(R.string.home_new_label_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${CardText.lot(card.weekStart)}  ${CardText.dateRange(card.weekStart, card.stats.weekEnd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { onOpenPublished(card.weekStart.toEpochDay()) },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.home_new_label_open)) }
            }
        }

        if (!hasPermission) {
            RuledBox {
                Text(text = stringResource(R.string.home_no_permission), style = MaterialTheme.typography.bodyMedium)
                SquareOutlinedButton(
                    text = stringResource(R.string.home_open_permissions),
                    onClick = onOpenPermissions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        val card = liveCard ?: sample
        ScaledCard(
            format = CardFormat.SQUARE,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = liveCard != null, onClick = onOpenResult),
        ) {
            LabelCardView(card, CardTemplate.CLASSIC, CardFormat.SQUARE, showWatermark = true)
        }
        Text(
            text = stringResource(if (liveCard != null) R.string.home_live_caption else R.string.home_sample_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (liveCard != null) {
            Button(onClick = onOpenResult, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_open_card))
            }
        }
    }
}
