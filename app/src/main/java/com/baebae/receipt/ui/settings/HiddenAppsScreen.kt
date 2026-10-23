package com.baebae.receipt.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.data.usage.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HiddenAppsScreen() {
    val container = appContainer()
    val apps by produceState<List<InstalledApps.App>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { container.installedApps.launcherApps() }
    }
    var hidden by remember { mutableStateOf(container.prefs.hiddenPackages) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(text = stringResource(R.string.settings_hidden_apps), style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = stringResource(R.string.hidden_apps_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        val list = apps
        if (list == null) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else {
            items(list, key = { it.packageName }) { app ->
                val isHidden = app.packageName in hidden
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            hidden = if (isHidden) hidden - app.packageName else hidden + app.packageName
                            container.prefs.hiddenPackages = hidden
                        },
                ) {
                    Checkbox(checked = isHidden, onCheckedChange = null)
                    Text(text = app.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
