package com.example.gpstracker.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gpstracker.MainActivity
import com.example.gpstracker.R

// Работает в фоновом режиме, работает на основном потоке
class LocationService : Service() {
    // Связывает сервис с activity
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Если система убьёт сервис, при появлении свободной памяти сервис перезапустится - START_STICKY
        startNotification()
        isRunning = true
        Log.d("MyLog1", "Service created")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyLog1", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog1", "onDestroy")
        isRunning = false
    }

    // TODO: Добавить вызов диалогового окна разрешение на показ уведомлений, иначе не работает фоновый сервис
        private fun startNotification() {
        // Для Android > 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT //IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }
        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            10,
            nIntent,
            PendingIntent.FLAG_IMMUTABLE // Был "0" в уроке NECO
        )
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker Running!")
            .setContentText("Service is running in the background.")
            .setContentIntent(pIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        startForeground(99, notification)

    }

    companion object{
        const val CHANNEL_ID = "channel_1"
        // для проверки в mainfragment сервис запущен или нет (глобальная переменная видна везде)
        var isRunning = false
    }
}