package com.baebae.receipt

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.baebae.receipt.notify.WeeklyNotifier
import com.baebae.receipt.work.CollectWorker
import com.baebae.receipt.work.PublishWorker

class ReceiptApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WeeklyNotifier.createChannel(this)
        CollectWorker.schedule(this)
        PublishWorker.schedule(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as ReceiptApplication).container

@Composable
fun appContainer(): AppContainer = LocalContext.current.appContainer
