package com.baebae.receipt.data.steps

import android.content.Context
import com.baebae.receipt.data.db.StepSnapshotDao
import com.baebae.receipt.data.db.StepSnapshotEntity

class StepRepository(
    private val context: Context,
    private val dao: StepSnapshotDao,
) {

    /** 앱을 열었을 때: 마지막 스냅샷이 [minIntervalMs]보다 오래됐으면 짧게 기다려 새로 남긴다. */
    suspend fun snapshotIfStale(minIntervalMs: Long = 5 * MINUTE) {
        val latest = dao.latest()
        if (latest != null && System.currentTimeMillis() - latest.timeMillis < minIntervalMs) return
        snapshot(StepSensor.FOREGROUND_TIMEOUT_MS)
    }

    suspend fun snapshot(timeoutMs: Long = StepSensor.BACKGROUND_TIMEOUT_MS): Boolean {
        val sample = StepSensor.read(context, timeoutMs) ?: return false
        dao.insert(StepSnapshotEntity(timeMillis = sample.timeMillis, counter = sample.counter, bootMillis = sample.bootMillis))
        dao.deleteOlderThan(sample.timeMillis - RETENTION_MS)
        return true
    }

    suspend fun stepsBetween(start: Long, end: Long): Long {
        // 구간 앞뒤로 여유를 두고 읽어야 경계를 걸친 스냅샷 쌍이 잡힌다.
        val samples = dao.between(start - MARGIN_MS, end + MARGIN_MS)
            .map { StepSample(it.timeMillis, it.counter, it.bootMillis) }
        return StepMath.stepsBetween(samples, start, end)
    }

    private companion object {
        const val MINUTE = 60_000L
        const val DAY = 24 * 60 * MINUTE
        const val MARGIN_MS = 3 * DAY
        const val RETENTION_MS = 60 * DAY
    }
}
