package com.arkiolabs.airpay.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Shake detector using the device's accelerometer.
 *
 * Detects deliberate shake motions to trigger actions like "Shake to Scan".
 * Uses gravity-normalized force calculation with a debounce window
 * to prevent false positives while walking or handling the phone normally.
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp: Long = 0L

    companion object {
        // Normalized g-force threshold: 2.7g represents a firm, intentional shake
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        // Debounce window to prevent multiple triggers in a single shake motion
        private const val SHAKE_DEBOUNCE_MS = 1500L
    }

    fun start(): Boolean {
        if (sensorManager == null || accelerometer == null) return false
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Calculate normalized G-force magnitude
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > SHAKE_DEBOUNCE_MS) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
