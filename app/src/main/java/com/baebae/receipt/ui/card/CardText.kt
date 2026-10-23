package com.baebae.receipt.ui.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.domain.Ingredient
import com.baebae.receipt.domain.IngredientKind
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** 카드에 찍히는 숫자·날짜 서식. 라벨처럼 단위를 붙여 건조하게 표기한다. */
internal object CardText {

    fun locale(): Locale = Locale.getDefault()

    @Composable
    fun duration(ms: Long): String {
        val totalMinutes = (ms / 60_000L).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours == 0 -> stringResource(R.string.fmt_minutes, minutes)
            minutes == 0 -> stringResource(R.string.fmt_hours, hours)
            else -> stringResource(R.string.fmt_hours_minutes, hours, minutes)
        }
    }

    fun count(value: Long): String = NumberFormat.getIntegerInstance(locale()).format(value)

    fun percent(value: Double): String = when {
        value < 1.0 -> String.format(Locale.ROOT, "%.1f%%", value)
        else -> "${value.roundToInt()}%"
    }

    @Composable
    fun ingredientName(ingredient: Ingredient): String = when (ingredient.kind) {
        IngredientKind.APP -> ingredient.name
        IngredientKind.SLEEP -> stringResource(R.string.ingredient_sleep)
        IngredientKind.WALK -> stringResource(R.string.ingredient_walk)
    }

    @Composable
    fun ingredients(list: List<Ingredient>): String {
        val parts = list.map { "${ingredientName(it)} ${percent(it.percent)}" }
        return parts.joinToString(stringResource(R.string.list_separator))
    }

    @Composable
    fun dateRange(start: LocalDate, end: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(stringResource(R.string.fmt_date_pattern), locale())
        return "${start.format(formatter)} ~ ${end.format(formatter)}"
    }

    /** 실제 제품의 잉크젯 로트 번호처럼 찍는 ISO 주차 표기. 예) LOT 2026-W43 */
    fun lot(start: LocalDate): String {
        val week = start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val year = start.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR)
        return String.format(Locale.ROOT, "LOT %d-W%02d", year, week)
    }

    /** 1일 기준치 대비 %. 기준치는 strings가 아닌 코드 상수(본 제품 자체 기준). */
    fun dailyValuePercent(total: Double, dailyReference: Double, days: Int): Int {
        if (days <= 0 || dailyReference <= 0) return 0
        return (total / (dailyReference * days) * 100).roundToInt()
    }
}

/** 영양정보 '1일 기준치'. 실제 권장량이 아니라 본 제품이 임의로 정한 값이며 카드 하단에 그렇게 명시한다. */
internal object DailyReference {
    const val SCREEN_MS = 3.0 * 60 * 60 * 1000
    const val UNLOCKS = 60.0
    const val STEPS = 10_000.0
    const val SLEEP_MS = 8.0 * 60 * 60 * 1000
}
