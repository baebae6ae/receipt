package com.baebae.receipt.ui.card

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R

/** 카드 논리 크기. 내보낼 때는 너비 1080px에 맞춰 밀도를 고정한다. */
val CardWidth: Dp = 360.dp
const val CardExportWidthPx = 1080

enum class CardFormat(val height: Dp, @StringRes val label: Int) {
    STORY(640.dp, R.string.card_format_story),
    SQUARE(360.dp, R.string.card_format_square),
}

class CardColors(
    val background: Color,
    val label: Color,
    val ink: Color,
    val rule: Color,
)

enum class CardTemplate(@StringRes val label: Int, val free: Boolean, val colors: CardColors) {
    CLASSIC(
        R.string.template_classic,
        free = true,
        CardColors(background = Color(0xFFEDEBE6), label = Color.White, ink = Color(0xFF111111), rule = Color(0xFF111111)),
    ),
    KRAFT(
        R.string.template_kraft,
        free = false,
        CardColors(background = Color(0xFFB8956A), label = Color(0xFFF7F1E6), ink = Color(0xFF2B2118), rule = Color(0xFF2B2118)),
    ),
    NIGHT(
        R.string.template_night,
        free = false,
        CardColors(background = Color(0xFF000000), label = Color(0xFF111111), ink = Color(0xFFF2F2F2), rule = Color(0xFFF2F2F2)),
    ),
}
