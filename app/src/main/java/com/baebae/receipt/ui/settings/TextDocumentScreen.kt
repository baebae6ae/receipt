package com.baebae.receipt.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baebae.receipt.R
import com.baebae.receipt.ui.components.SectionTitle
import com.baebae.receipt.ui.theme.Mono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PrivacyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = stringResource(R.string.settings_privacy), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.privacy_body), style = MaterialTheme.typography.bodyMedium)
    }
}

private val licenseFiles = listOf(
    "Pretendard" to "licenses/Pretendard-OFL.txt",
    "JetBrains Mono" to "licenses/JetBrainsMono-OFL.txt",
)

@Composable
fun LicensesScreen() {
    val context = LocalContext.current
    val texts by produceState(initialValue = emptyList<Pair<String, String>>()) {
        value = withContext(Dispatchers.IO) {
            licenseFiles.map { (name, path) ->
                name to context.assets.open(path).bufferedReader().use { it.readText() }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = stringResource(R.string.settings_licenses), style = MaterialTheme.typography.headlineMedium)
        Text(text = stringResource(R.string.licenses_intro), style = MaterialTheme.typography.bodySmall)
        texts.forEach { (name, text) ->
            SectionTitle(name)
            Text(text = text, style = TextStyle(fontFamily = Mono, fontSize = 10.sp, lineHeight = 14.sp))
        }
    }
}
