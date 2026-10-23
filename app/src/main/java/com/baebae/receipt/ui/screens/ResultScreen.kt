package com.baebae.receipt.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R

// 결과 카드 화면에는 광고를 넣지 않는다(배너 포함).
@Composable
fun ResultScreen() {
    ScreenFrame(title = stringResource(R.string.result_title))
}
