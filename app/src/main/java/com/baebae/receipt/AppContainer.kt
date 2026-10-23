package com.baebae.receipt

import android.content.Context
import com.baebae.receipt.data.AppPrefs

/** 수동 주입 컨테이너. 앱 전체에서 하나만 만든다. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val prefs = AppPrefs(appContext)
}
