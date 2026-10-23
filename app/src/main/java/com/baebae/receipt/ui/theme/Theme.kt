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
)

@Composable
fun ReceiptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReceiptColors,
        typography = ReceiptTypography,
        content = content,
    )
}
