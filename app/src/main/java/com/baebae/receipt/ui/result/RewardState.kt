package com.baebae.receipt.ui.result

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.baebae.receipt.R
import com.baebae.receipt.ads.AdsManager
import com.baebae.receipt.appContainer
import com.baebae.receipt.data.AppPrefs
import com.baebae.receipt.ui.card.CardTemplate

/**
 * 보상형 광고로 얻는 것: 추가 템플릿(영구 해제), 워터마크 제거(광고 1회 = 내보내기 1회).
 * 광고는 사용자가 확인을 누른 경우에만 띄운다.
 */
@Stable
class RewardState(
    private val prefs: AppPrefs,
    private val ads: AdsManager,
    private val activity: Activity?,
    private val failedMessage: String,
) {
    var unlocked by mutableStateOf(prefs.unlockedTemplates)
        private set
    var credits by mutableStateOf(prefs.watermarkCredits)
        private set
    var removeWatermark by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set

    /** 이 화면에서 이미 1회를 썼으면 같은 카드는 저장·공유를 여러 번 해도 더 차감하지 않는다. */
    private var paidThisSession = false

    internal var unlockRequest by mutableStateOf<Pair<CardTemplate, () -> Unit>?>(null)
    internal var watermarkRequest by mutableStateOf(false)

    fun isAvailable(template: CardTemplate) = template.free || template.name in unlocked

    fun askUnlock(template: CardTemplate, onUnlocked: () -> Unit) {
        unlockRequest = template to onUnlocked
    }

    fun setWatermarkRemoved(remove: Boolean) {
        if (!remove) {
            removeWatermark = false
        } else if (credits > 0 || paidThisSession) {
            removeWatermark = true
        } else {
            watermarkRequest = true
        }
    }

    fun onExported() {
        if (!removeWatermark || paidThisSession) return
        prefs.watermarkCredits = prefs.watermarkCredits - 1
        credits = prefs.watermarkCredits
        paidThisSession = true
    }

    internal fun watchForUnlock() {
        val (template, onUnlocked) = unlockRequest ?: return
        unlockRequest = null
        watch {
            prefs.unlockedTemplates = prefs.unlockedTemplates + template.name
            unlocked = prefs.unlockedTemplates
            onUnlocked()
        }
    }

    internal fun watchForWatermark() {
        watermarkRequest = false
        watch {
            prefs.watermarkCredits = prefs.watermarkCredits + 1
            credits = prefs.watermarkCredits
            removeWatermark = true
        }
    }

    private fun watch(onRewarded: () -> Unit) {
        val host = activity ?: return
        loading = true
        ads.showRewarded(
            host,
            onRewarded = onRewarded,
            onFinished = { rewarded ->
                loading = false
                if (!rewarded) Toast.makeText(host, failedMessage, Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
fun rememberRewardState(): RewardState {
    val container = appContainer()
    val activity = LocalActivity.current
    val failed = stringResource(R.string.ad_reward_failed)
    return remember(activity) { RewardState(container.prefs, container.ads, activity, failed) }
}

@Composable
fun WatermarkToggle(state: RewardState) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.watermark_remove), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.watermark_credits, state.credits),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = state.removeWatermark,
            onCheckedChange = { state.setWatermarkRemoved(it) },
            enabled = !state.loading,
        )
    }
}

@Composable
fun RewardDialogs(state: RewardState) {
    val context = LocalContext.current
    state.unlockRequest?.let { (template, _) ->
        AlertDialog(
            onDismissRequest = { state.unlockRequest = null },
            title = { Text(stringResource(R.string.reward_unlock_title, stringResource(template.label))) },
            text = { Text(stringResource(R.string.reward_unlock_text)) },
            confirmButton = { TextButton(onClick = { state.watchForUnlock() }) { Text(stringResource(R.string.reward_watch)) } },
            dismissButton = { TextButton(onClick = { state.unlockRequest = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    if (state.watermarkRequest) {
        AlertDialog(
            onDismissRequest = { state.watermarkRequest = false },
            title = { Text(stringResource(R.string.watermark_remove)) },
            text = { Text(stringResource(R.string.reward_watermark_text)) },
            confirmButton = { TextButton(onClick = { state.watchForWatermark() }) { Text(stringResource(R.string.reward_watch)) } },
            dismissButton = { TextButton(onClick = { state.watermarkRequest = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    if (state.loading) {
        // 광고를 불러오는 동안 짧게 안내만 한다.
        LaunchedToast(context, stringResource(R.string.ad_loading))
    }
}

@Composable
private fun LaunchedToast(context: Context, message: String) {
    LaunchedEffect(message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
