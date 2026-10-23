package com.baebae.receipt.ui.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.baebae.receipt.R
import com.baebae.receipt.data.usage.UsagePermission
import com.baebae.receipt.ui.components.RuledBox
import com.baebae.receipt.ui.components.SquareOutlinedButton

@Composable
fun PermissionsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var usageGranted by remember { mutableStateOf(UsagePermission.isGranted(context)) }

    // 설정 화면에서 돌아올 때마다 다시 확인한다.
    LifecycleResumeEffect(Unit) {
        usageGranted = UsagePermission.isGranted(context)
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.permissions_title), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.permissions_intro), style = MaterialTheme.typography.bodyMedium)

        PermissionItem(
            title = stringResource(R.string.permission_usage_title),
            description = stringResource(R.string.permission_usage_desc),
            granted = usageGranted,
            actionLabel = stringResource(R.string.permission_open_settings),
            onRequest = { UsagePermission.openSettings(context) },
        )

        Button(onClick = onDone, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.permissions_done))
        }
    }
}

@Composable
internal fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    onRequest: () -> Unit,
) {
    RuledBox {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(if (granted) R.string.permission_state_on else R.string.permission_state_off),
                style = MaterialTheme.typography.labelLarge,
                color = if (granted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!granted) {
            SquareOutlinedButton(text = actionLabel, onClick = onRequest, modifier = Modifier.fillMaxWidth())
        }
    }
}
