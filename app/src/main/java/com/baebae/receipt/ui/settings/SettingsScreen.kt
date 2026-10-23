package com.baebae.receipt.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.data.AppPrefs
import com.baebae.receipt.ui.components.LabelRow
import com.baebae.receipt.ui.components.SectionTitle

@Composable
fun SettingsScreen(
    onOpenPermissions: () -> Unit,
    onOpenHiddenApps: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
    bottomContent: @Composable () -> Unit = {},
) {
    val prefs = appContainer().prefs
    var nickname by remember { mutableStateOf(prefs.nickname) }
    var editingName by remember { mutableStateOf(false) }
    val hiddenCount = remember { prefs.hiddenPackages.size }
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

            SectionTitle(stringResource(R.string.settings_section_card))
            LabelRow(
                title = stringResource(R.string.settings_nickname),
                description = stringResource(R.string.settings_nickname_desc),
                value = nickname.ifEmpty { stringResource(R.string.default_nickname) },
                onClick = { editingName = true },
            )
            LabelRow(
                title = stringResource(R.string.settings_hidden_apps),
                description = stringResource(R.string.settings_hidden_apps_desc),
                value = stringResource(R.string.settings_hidden_apps_count, hiddenCount),
                onClick = onOpenHiddenApps,
            )

            SectionTitle(stringResource(R.string.settings_section_data))
            LabelRow(
                title = stringResource(R.string.settings_permissions),
                description = stringResource(R.string.settings_permissions_desc),
                onClick = onOpenPermissions,
            )
            LabelRow(title = stringResource(R.string.settings_privacy), onClick = onOpenPrivacy)

            SectionTitle(stringResource(R.string.settings_section_info))
            LabelRow(title = stringResource(R.string.settings_licenses), onClick = onOpenLicenses)
            LabelRow(title = stringResource(R.string.settings_version), value = version)
        }
        bottomContent()
    }

    if (editingName) {
        NicknameDialog(
            initial = nickname,
            onDismiss = { editingName = false },
            onSave = {
                prefs.nickname = it
                nickname = prefs.nickname
                editingName = false
            },
        )
    }
}

@Composable
private fun NicknameDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_nickname)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(AppPrefs.MAX_NICKNAME) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.default_nickname)) },
            )
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text(stringResource(R.string.action_ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
