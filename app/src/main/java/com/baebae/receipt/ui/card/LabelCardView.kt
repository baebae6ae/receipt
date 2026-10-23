package com.baebae.receipt.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baebae.receipt.R
import com.baebae.receipt.domain.ItemKeys
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.ui.theme.Mono
import com.baebae.receipt.ui.theme.Pretendard

/**
 * 성분표 카드. 크기는 [CardWidth] x [CardFormat.height]로 고정이고,
 * 화면 미리보기와 이미지 내보내기 모두 [ScaledCard]가 이 크기를 비율에 맞게 축소/확대한다.
 */
@Composable
fun LabelCardView(
    card: LabelCard,
    template: CardTemplate,
    format: CardFormat,
    showWatermark: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = template.colors
    CompositionLocalProvider(LocalContentColor provides colors.ink) {
        Box(
            modifier = modifier
                .size(CardWidth, format.height)
                .background(colors.background),
        ) {
            when (format) {
                CardFormat.STORY -> StoryLayout(card, colors, showWatermark)
                CardFormat.SQUARE -> SquareLayout(card, colors, showWatermark)
            }
        }
    }
}

private class CardSizes(
    val title: TextUnit,
    val body: TextUnit,
    val bodyLine: TextUnit,
    val small: TextUnit,
    val smallLine: TextUnit,
    val nutrition: TextUnit,
    val header: TextUnit,
    val cellPadding: Dp,
    val rule: Dp,
    val thickRule: Dp,
)

private val StorySizes = CardSizes(
    title = 22.sp, body = 10.sp, bodyLine = 14.5.sp, small = 7.5.sp, smallLine = 10.5.sp,
    nutrition = 10.5.sp, header = 15.sp, cellPadding = 6.dp,
    rule = 0.6.dp, thickRule = 2.dp,
)

private val SquareSizes = CardSizes(
    title = 16.sp, body = 9.sp, bodyLine = 13.sp, small = 6.6.sp, smallLine = 9.4.sp,
    nutrition = 10.sp, header = 13.sp, cellPadding = 5.dp,
    rule = 0.6.dp, thickRule = 1.8.dp,
)

@Composable
private fun StoryLayout(card: LabelCard, colors: CardColors, showWatermark: Boolean) {
    val sizes = StorySizes
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.label)
                .border(1.5.dp, colors.rule)
                .padding(14.dp),
        ) {
            ProductHeader(card, sizes)
            Spacer(Modifier.height(10.dp))
            InfoTable(card, colors, sizes, compact = false)
            if (hasNutrition(card)) {
                Spacer(Modifier.height(12.dp))
                NutritionBox(card, colors, sizes)
            }
        }
        if (showWatermark) {
            Spacer(Modifier.height(12.dp))
            Watermark(sizes)
        }
    }
}

@Composable
private fun SquareLayout(card: LabelCard, colors: CardColors, showWatermark: Boolean) {
    val sizes = SquareSizes
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.label)
                .border(1.5.dp, colors.rule)
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            ProductHeader(card, sizes)
            Spacer(Modifier.height(8.dp))
            Row {
                InfoTable(card, colors, sizes, compact = true, modifier = Modifier.weight(1.15f))
                if (hasNutrition(card)) {
                    Spacer(Modifier.width(7.dp))
                    NutritionBox(card, colors, sizes, modifier = Modifier.weight(1f))
                }
            }
        }
        if (showWatermark) {
            Spacer(Modifier.height(6.dp))
            Watermark(sizes)
        }
    }
}

@Composable
private fun ProductHeader(card: LabelCard, sizes: CardSizes) {
    Text(
        text = stringResource(R.string.card_product_name, card.productName),
        style = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.ExtraBold,
            fontSize = sizes.title,
            lineHeight = sizes.title * 1.25f,
        ),
    )
    Text(
        text = "${CardText.lot(card.periodStart)}  ${CardText.dateRange(card.periodStart, card.periodEnd)}",
        style = TextStyle(fontFamily = Mono, fontSize = sizes.small, lineHeight = sizes.smallLine),
    )
}

private class InfoRow(val label: String, val value: String)

@Composable
private fun InfoTable(
    card: LabelCard,
    colors: CardColors,
    sizes: CardSizes,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val rows = buildList {
        if (!compact) {
            add(InfoRow(stringResource(R.string.card_row_product), stringResource(R.string.card_product_name, card.productName)))
            add(InfoRow(stringResource(R.string.card_row_food_type), stringResource(R.string.card_food_type_value)))
            add(InfoRow(stringResource(R.string.card_row_content), stringResource(R.string.fmt_days, card.nutrition.days)))
        }
        val ingredients = card.visibleIngredients
        if (ingredients.isNotEmpty()) {
            add(InfoRow(stringResource(R.string.card_row_ingredients), CardText.ingredients(ingredients)))
        }
        if (card.isShown(ItemKeys.STORAGE) && card.storage != null) {
            add(InfoRow(stringResource(R.string.card_row_storage), card.storage.text))
        }
        if (card.isShown(ItemKeys.CAUTION) && card.cautions.isNotEmpty()) {
            val cautions = if (compact) card.cautions.take(1) else card.cautions
            add(InfoRow(stringResource(R.string.card_row_caution), cautions.joinToString(" ") { it.text }))
        }
        if (card.isShown(ItemKeys.EXPIRY) && card.expiry != null) {
            add(InfoRow(stringResource(R.string.card_row_expiry), card.expiry.text))
        }
    }
    if (rows.isEmpty()) return

    val labelColumn = dimensionResource(
        if (compact) R.dimen.card_label_column_square else R.dimen.card_label_column_story,
    )

    val labelStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = sizes.body,
        lineHeight = sizes.bodyLine,
    )
    val valueStyle = labelStyle.copy(fontWeight = FontWeight.Normal)
    Column(modifier = modifier.border(sizes.rule, colors.rule)) {
        rows.forEachIndexed { index, row ->
            if (index > 0) Rule(colors.rule, sizes.rule)
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Text(
                    text = row.label,
                    style = labelStyle,
                    modifier = Modifier
                        .width(labelColumn)
                        .padding(sizes.cellPadding),
                )
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(sizes.rule)
                        .background(colors.rule),
                )
                Text(
                    text = row.value,
                    style = valueStyle,
                    maxLines = if (compact) 5 else 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(sizes.cellPadding),
                )
            }
        }
    }
}

private class NutritionRow(val name: String, val amount: String, val percent: Int?)

private fun hasNutrition(card: LabelCard) =
    listOf(ItemKeys.SCREEN, ItemKeys.UNLOCKS, ItemKeys.STEPS, ItemKeys.SLEEP).any(card::isShown)

/** 2021년 개정 '영양정보' 표시 서식(검은 머리띠, 총 내용량당 / 1일 기준치 비율)을 그대로 따른다. */
@Composable
private fun NutritionBox(
    card: LabelCard,
    colors: CardColors,
    sizes: CardSizes,
    modifier: Modifier = Modifier,
) {
    val n = card.nutrition
    val rows = buildList {
        if (card.isShown(ItemKeys.SCREEN)) add(
            NutritionRow(
                stringResource(R.string.nutrition_screen_time),
                CardText.duration(n.screenMs),
                CardText.dailyValuePercent(n.screenMs.toDouble(), DailyReference.SCREEN_MS, n.days),
            ),
        )
        if (card.isShown(ItemKeys.UNLOCKS)) add(
            NutritionRow(
                stringResource(R.string.nutrition_unlocks),
                stringResource(R.string.fmt_times, CardText.count(n.unlocks.toLong())),
                CardText.dailyValuePercent(n.unlocks.toDouble(), DailyReference.UNLOCKS, n.days),
            ),
        )
        if (card.isShown(ItemKeys.STEPS)) add(
            NutritionRow(
                stringResource(R.string.nutrition_steps),
                stringResource(R.string.fmt_steps, CardText.count(n.steps)),
                CardText.dailyValuePercent(n.steps.toDouble(), DailyReference.STEPS, n.days),
            ),
        )
        if (card.isShown(ItemKeys.SLEEP)) add(
            NutritionRow(
                stringResource(R.string.nutrition_sleep),
                n.avgSleepMs?.let { CardText.duration(it) } ?: stringResource(R.string.nutrition_not_measured),
                n.avgSleepMs?.let { CardText.dailyValuePercent(it.toDouble(), DailyReference.SLEEP_MS, 1) },
            ),
        )
    }

    val base = TextStyle(fontFamily = Pretendard, fontSize = sizes.nutrition, lineHeight = sizes.nutrition * 1.35f)
    Column(modifier = modifier.border(sizes.rule * 1.5f, colors.rule)) {
        // 머리띠: 잉크색 바탕에 라벨색 글씨
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.ink)
                .padding(horizontal = sizes.cellPadding * 1.2f, vertical = sizes.cellPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.nutrition_title),
                color = colors.label,
                style = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = sizes.header, lineHeight = sizes.header * 1.2f),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.nutrition_total_content, stringResource(R.string.fmt_days, n.days)),
                color = colors.label,
                style = base.copy(fontSize = sizes.small, lineHeight = sizes.smallLine),
                textAlign = TextAlign.End,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sizes.cellPadding * 1.2f, vertical = sizes.cellPadding * 0.7f),
        ) {
            Text(
                text = stringResource(R.string.nutrition_per_total),
                style = base.copy(fontWeight = FontWeight.SemiBold, fontSize = sizes.small, lineHeight = sizes.smallLine),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.nutrition_daily_value_header),
                style = base.copy(fontWeight = FontWeight.SemiBold, fontSize = sizes.small, lineHeight = sizes.smallLine),
                textAlign = TextAlign.End,
            )
        }
        Rule(colors.rule, sizes.thickRule)
        rows.forEachIndexed { index, row ->
            if (index > 0) Rule(colors.rule, sizes.rule)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sizes.cellPadding * 1.2f, vertical = sizes.cellPadding * 0.8f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = row.name, style = base.copy(fontWeight = FontWeight.SemiBold))
                Text(
                    text = " ${row.amount}",
                    style = base,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Text(
                    text = row.percent?.let { "$it%" } ?: "-",
                    style = base.copy(fontWeight = FontWeight.ExtraBold),
                )
            }
        }
        Rule(colors.rule, sizes.thickRule)
        Text(
            text = stringResource(R.string.nutrition_footnote),
            style = base.copy(fontSize = sizes.small, lineHeight = sizes.smallLine),
            modifier = Modifier.padding(horizontal = sizes.cellPadding * 1.2f, vertical = sizes.cellPadding * 0.8f),
        )
    }
}

@Composable
private fun Rule(color: Color, thickness: Dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}

@Composable
private fun Watermark(sizes: CardSizes) {
    Text(
        text = stringResource(R.string.card_watermark),
        style = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = sizes.small,
            lineHeight = sizes.smallLine,
            letterSpacing = 0.5.sp,
        ),
    )
}
