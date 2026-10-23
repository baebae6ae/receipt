package com.baebae.receipt.data.steps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** TYPE_STEP_COUNTER 를 한 번 읽는다. 값은 마지막 부팅 이후 누적 걸음 수. */
object StepSensor {

    /** 백그라운드 작업에서는 넉넉히 기다린다. */
    const val BACKGROUND_TIMEOUT_MS = 10_000L

    /** 화면을 막지 않도록 앱을 열었을 때는 짧게만 기다린다. */
    const val FOREGROUND_TIMEOUT_MS = 1_500L

    fun isAvailable(context: Context): Boolean =
        context.getSystemService(SensorManager::class.java)?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

    /**
     * 센서가 없거나 권한이 없거나 제한 시간 안에 값이 오지 않으면 null.
     * 백그라운드에서는 기기에 따라 이벤트가 오지 않을 수 있어 앱을 열 때도 스냅샷을 남긴다.
     */
    suspend fun read(context: Context, timeoutMs: Long = BACKGROUND_TIMEOUT_MS): StepSample? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(SensorManager::class.java) ?: return null
        val sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null
        val counter = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<Long> { cont ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        manager.unregisterListener(this)
                        if (cont.isActive) cont.resume(event.values[0].toLong())
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                cont.invokeOnCancellation { manager.unregisterListener(listener) }
            }
        } ?: return null
        val now = System.currentTimeMillis()
        return StepSample(timeMillis = now, counter = counter, bootMillis = now - SystemClock.elapsedRealtime())
    }
}
