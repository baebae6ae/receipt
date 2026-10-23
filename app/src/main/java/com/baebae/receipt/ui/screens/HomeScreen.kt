package com.baebae.receipt.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R

@Composable
fun HomeScreen(onOpenResult: () -> Unit) {
    ScreenFrame(title = stringResource(R.string.home_title)) {
        // 결과 카드 화면 이동을 확인하기 위한 임시 진입점. 홈 미리보기 구현 시 교체.
        OutlinedButton(onClick = onOpenResult, shape = RoundedCornerShape(0.dp)) {
            Text(stringResource(R.string.result_title))
        }
    }
}
