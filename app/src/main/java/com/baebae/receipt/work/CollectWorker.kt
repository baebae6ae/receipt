package com.baebae.receipt.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.baebae.receipt.appContainer
import java.util.concurrent.TimeUnit

/** 1시간마다 걸음 센서 스냅샷을 남기고 최근 이틀의 사용 요약을 캐시한다. 네트워크를 쓰지 않는다. */
class CollectWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = applicationContext.appContainer
        container.steps.snapshot()
        container.stats.refreshRecentDays()
        return Result.success()
    }

    companion object {
        private const val NAME = "collect"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<CollectWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
