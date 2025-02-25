package com.example.gpstracker.stepcounter

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class StepCounterService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager // получаем данные от датчиков
    private var stepCounterSensor: Sensor? = null // сенсор шагомер
    private var initialSteps: Int = -1 // значения шагов в момент запуска
    private var currentSteps: Int = 0 // количество шагов, подсчитанное после запуска.

    override fun onCreate() {
        super.onCreate()
        Log.d("StepService", "Сервис запущен")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepService", "Сенсор подключен")
        } else {
            Log.e("StepService", "Сенсор шагов отсутствует")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialSteps == -1) {
                initialSteps = event.values[0].toInt() // Запоминаем начальное значение
            }
            currentSteps = event.values[0].toInt() - initialSteps
            Log.d("StepService", "Обнаружено движение! Шагов: $currentSteps")
            sendStepUpdate(currentSteps)


        }
    }

    private fun sendStepUpdate(steps: Int) {
        val intent = Intent("STEP_UPDATE")
        intent.putExtra("step_count", steps)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}