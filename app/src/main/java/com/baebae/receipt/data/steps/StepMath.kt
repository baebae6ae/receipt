package com.baebae.receipt.data.steps

import kotlin.math.abs
import kotlin.math.roundToLong

data class StepSample(val timeMillis: Long, val counter: Long, val bootMillis: Long)

object StepMath {

    /** 부팅 시각 추정 오차 허용치. 이보다 크게 달라지면 재부팅으로 본다. */
    private const val BOOT_TOLERANCE_MS = 60_000L

    /**
     * [samples](시간순) 사이의 걸음을 시간에 비례해 나눠 [start, end) 구간에 떨어지는 양을 구한다.
     * 재부팅이 끼면 부팅 이후 누적값만 센다(재부팅 직전 마지막 스냅샷 이후 걸음은 잃는다).
     */
    fun stepsBetween(samples: List<StepSample>, start: Long, end: Long): Long {
        if (end <= start) return 0
        var total = 0.0
        for ((a, b) in samples.zipWithNext()) {
            val rebooted = abs(b.bootMillis - a.bootMillis) > BOOT_TOLERANCE_MS || b.counter < a.counter
            val from = if (rebooted) maxOf(a.timeMillis, b.bootMillis) else a.timeMillis
            val steps = if (rebooted) b.counter else b.counter - a.counter
            if (steps <= 0 || b.timeMillis <= from) continue
            val overlap = minOf(b.timeMillis, end) - maxOf(from, start)
            if (overlap > 0) total += steps * overlap.toDouble() / (b.timeMillis - from)
        }
        return total.roundToLong()
    }
}
