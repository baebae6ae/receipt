package com.baebae.receipt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R

/** 각 화면을 구현하기 전까지 쓰는 공통 뼈대. 제목 + 괘선 + 본문 슬롯. */
@Composable
internal fun ScreenFrame(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = { ComingSoon() },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        content()
    }
}

@Composable
private fun ComingSoon() {
    Text(
        text = stringResource(R.string.placeholder_coming_soon),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
