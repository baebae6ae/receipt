package com.baebae.receipt

import android.content.Context
import com.baebae.receipt.data.AppPrefs
import com.baebae.receipt.data.LabelService
import com.baebae.receipt.data.StatsRepository
import com.baebae.receipt.data.usage.InstalledApps
import com.baebae.receipt.data.usage.UsageCollector

/** 수동 주입 컨테이너. 앱 전체에서 하나만 만든다. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val prefs = AppPrefs(appContext)
    val installedApps = InstalledApps(appContext)
    val stats = StatsRepository(appContext, UsageCollector(appContext), installedApps)
    val labels = LabelService(appContext, prefs, stats)
}
