package com.baebae.receipt.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.baebae.receipt.data.steps.StepSensor
import com.baebae.receipt.data.usage.UsagePermission
import com.baebae.receipt.notify.WeeklyNotifier
import com.baebae.receipt.ui.components.RuledBox
import com.baebae.receipt.ui.components.SquareOutlinedButton

@Composable
fun PermissionsScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var usageGranted by remember { mutableStateOf(UsagePermission.isGranted(context)) }
    var activityGranted by remember { mutableStateOf(StepSensor.hasPermission(context)) }
    // 한 번 거절되면 시스템 대화상자가 다시 뜨지 않을 수 있어 앱 정보 화면으로 보낸다.
    var activityDenied by remember { mutableStateOf(false) }
    val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        activityGranted = granted
        activityDenied = !granted
    }

    var notifyGranted by remember { mutableStateOf(WeeklyNotifier.canNotify(context)) }
    var notifyDenied by remember { mutableStateOf(false) }
    val notifyLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifyGranted = WeeklyNotifier.canNotify(context)
        notifyDenied = !granted
    }

    // 설정 화면에서 돌아올 때마다 다시 확인한다.
    LifecycleResumeEffect(Unit) {
        usageGranted = UsagePermission.isGranted(context)
        activityGranted = StepSensor.hasPermission(context)
        notifyGranted = WeeklyNotifier.canNotify(context)
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

        if (StepSensor.isAvailable(context)) {
            PermissionItem(
                title = stringResource(R.string.permission_activity_title),
                description = stringResource(R.string.permission_activity_desc),
                granted = activityGranted,
                actionLabel = stringResource(if (activityDenied) R.string.permission_open_settings else R.string.permission_allow),
                onRequest = {
                    if (activityDenied || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        openAppDetails(context)
                    } else {
                        activityLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                },
            )
        } else {
            RuledBox {
                Text(text = stringResource(R.string.permission_activity_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.permission_activity_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        PermissionItem(
            title = stringResource(R.string.permission_notification_title),
            description = stringResource(R.string.permission_notification_desc),
            granted = notifyGranted,
            actionLabel = stringResource(if (notifyDenied) R.string.permission_open_settings else R.string.permission_allow),
            onRequest = {
                if (!notifyDenied && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notifyLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    openNotificationSettings(context)
                }
            },
        )

        Button(onClick = onDone, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.permissions_done))
        }
    }
}

internal fun openAppDetails(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

internal fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure { openAppDetails(context) }
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
