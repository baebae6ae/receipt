package com.baebae.receipt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 실제 식품 라벨처럼 흑백 기반. 다크 테마는 두지 않는다(카드 이미지와 같은 종이/잉크 느낌 유지).
private val ReceiptColors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = InkSoft,
    onSecondary = Paper,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = InkSoft,
    outline = Ink,
    outlineVariant = Rule,
    // 기본값이 보라 계열이라 칩·스위치·대화상자 색이 흑백에서 벗어난다. 컨테이너 색을 모두 지정한다.
    primaryContainer = PaperDim,
    onPrimaryContainer = Ink,
    secondaryContainer = Ink,
    onSecondaryContainer = Paper,
    tertiary = Ink,
    onTertiary = Paper,
    tertiaryContainer = PaperDim,
    onTertiaryContainer = Ink,
    surfaceTint = Paper,
    surfaceDim = PaperDim,
    surfaceBright = Paper,
    surfaceContainerLowest = Paper,
    surfaceContainerLow = Paper,
    surfaceContainer = Paper,
    surfaceContainerHigh = Paper,
    surfaceContainerHighest = PaperDim,
)

@Composable
fun ReceiptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReceiptColors,
        typography = ReceiptTypography,
        content = content,
    )
}
