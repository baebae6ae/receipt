package com.baebae.receipt

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.baebae.receipt.work.CollectWorker

class ReceiptApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CollectWorker.schedule(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as ReceiptApplication).container

@Composable
fun appContainer(): AppContainer = LocalContext.current.appContainer
