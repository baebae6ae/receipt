package com.baebae.receipt.ui.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import kotlin.math.roundToInt

/**
 * 카드를 항상 너비 [CardExportWidthPx] px 기준으로 배치한 뒤, 주어진 너비에 맞게 축소해 그린다.
 * 화면 밀도나 글꼴 크기 설정과 무관하게 미리보기와 내보낸 이미지가 같은 모양이 된다.
 * [contentModifier]는 축소 전(1080px 기준) 카드 노드에 붙는다. 이미지 캡처용 그래픽 레이어를 여기에 단다.
 */
@Composable
fun ScaledCard(
    format: CardFormat,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val exportDensity = Density(density = CardExportWidthPx / CardWidth.value, fontScale = 1f)
    val heightPx = (format.height.value * exportDensity.density).roundToInt()
    Box(
        modifier = modifier
            .aspectRatio(CardWidth.value / format.height.value)
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(Constraints.fixed(CardExportWidthPx, heightPx))
                val scale = constraints.maxWidth / CardExportWidthPx.toFloat()
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeWithLayer(0, 0) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                }
            }
            .then(contentModifier),
    ) {
        CompositionLocalProvider(LocalDensity provides exportDensity) {
            content()
        }
    }
}
