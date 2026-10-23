package com.baebae.receipt

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.baebae.receipt.notify.WeeklyNotifier
import com.baebae.receipt.ui.navigation.ReceiptApp
import com.baebae.receipt.ui.theme.ReceiptTheme

class MainActivity : ComponentActivity() {

    /** 알림으로 들어왔을 때 열어야 할 주(epochDay). */
    private val pendingWeek = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱은 항상 밝은 종이 테마라, 시스템이 다크 모드여도 상태바 아이콘을 어둡게 둔다.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        appContainer.ads.initialize(this)
        if (savedInstanceState == null) pendingWeek.value = weekFrom(intent)
        setContent {
            ReceiptTheme {
                ReceiptApp(
                    pendingWeek = pendingWeek.value,
                    onPendingWeekHandled = { pendingWeek.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        weekFrom(intent)?.let { pendingWeek.value = it }
    }

    private fun weekFrom(intent: Intent?): Long? =
        intent?.takeIf { it.hasExtra(WeeklyNotifier.EXTRA_WEEK) }?.getLongExtra(WeeklyNotifier.EXTRA_WEEK, 0L)
}
