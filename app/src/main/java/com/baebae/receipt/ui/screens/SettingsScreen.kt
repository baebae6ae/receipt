package com.baebae.receipt.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.ui.components.LabelRow

@Composable
fun SettingsScreen(onOpenPermissions: () -> Unit) {
    ScreenFrame(title = stringResource(R.string.settings_title)) {
        LabelRow(
            title = stringResource(R.string.settings_permissions),
            description = stringResource(R.string.settings_permissions_desc),
            onClick = onOpenPermissions,
        )
    }
}
