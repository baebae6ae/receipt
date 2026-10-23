package com.baebae.receipt.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.baebae.receipt.appContainer
import com.baebae.receipt.notify.WeeklyNotifier
import com.baebae.receipt.util.WeekMath
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * 일요일 20:00 에 그 주 카드를 확정해 저장하고 알린다.
 * 실행이 늦어져도 [WeekMath.lastDueWeek]로 대상 주를 정하므로 결과는 같다.
 * 다음 주 예약은 실행 중인 자기 자신을 덮어쓰지 않도록 [CollectWorker]와 앱 시작 시 [schedule]이 맡는다.
 */
class PublishWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        publishDue(applicationContext, notify = true)
        return Result.success()
    }

    companion object {
        private const val NAME = "publish"

        /** 아직 발행되지 않은 '지난 발행 시각'의 주를 발행한다. 새로 발행했으면 true. */
        suspend fun publishDue(context: Context, notify: Boolean, now: ZonedDateTime = ZonedDateTime.now()): Boolean {
            val cards = context.appContainer.cards
            val week = WeekMath.lastDueWeek(now)
            if (cards.get(week) != null) return false
            cards.publish(week, now) ?: return false
            if (notify) WeeklyNotifier.notifyPublished(context, week)
            return true
        }

        /** 다음 발행 시각에 한 번 실행되도록 예약한다. 이미 대기 중이면 그대로 둔다. */
        fun schedule(context: Context) {
            val delay = Duration.between(ZonedDateTime.now(), WeekMath.nextPublishAt()).toMillis().coerceAtLeast(0)
            val request = OneTimeWorkRequestBuilder<PublishWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
