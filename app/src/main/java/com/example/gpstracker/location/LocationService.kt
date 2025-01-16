package com.example.gpstracker.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpstracker.MainActivity
import com.example.gpstracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import org.osmdroid.util.GeoPoint

// Работает в фоновом режиме, работает на основном потоке
class LocationService : Service() {

    private var distance = 0.0f
    private var lastLocation: Location? = null
    // Класс, который дает возможность получать сведения о местоположении
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest
    private lateinit var geoPointsList: ArrayList<GeoPoint>
    // Связывает сервис с activity
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Если система убьёт сервис, при появлении свободной памяти сервис перезапустится - START_STICKY
        startNotification()
        startLocationUpdates()
        isRunning = true
        Log.d("MyLog1", "Service created")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyLog1", "onCreate")
        geoPointsList = ArrayList()
        initLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog1", "onDestroy")
        isRunning = false
        locProvider.removeLocationUpdates(locCallBack)
    }

    private var locCallBack = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocation = lResult.lastLocation
            if (lastLocation != null && currentLocation != null) {
                // Из-за не большой неточности показаний GPS (дрожания на одном месте) данные дистанции все время будут добавляться.
                // Если скорость не меняется больше значения (высчитать экспериментально или сделать настройки), то суммируем новые значения дистанции
                //if (currentLocation.speed > 0.2) {
                distance += lastLocation?.distanceTo(currentLocation)!! // (currentLocation ?: lastLocation)!! / ?: 0.0f оператор элвиса "?:"
                //}
                geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude))
                val locModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    geoPointsList
                )
                sendLocData(locModel)
            }
            lastLocation = currentLocation
//            Log.d("MylogLoc", "${lResult.lastLocation?.latitude}")
            Log.d("MylogLoc", "$distance")
        }
    }

    // Передача данных LocationModel в MainFragment
    private fun sendLocData(locModel: LocationModel) {
        // Создаем Intent
        val i = Intent(LOC_MODEL_INTENT)
        // Помещаем в Intent данные
        i.putExtra(LOC_MODEL_INTENT, locModel)
        // Передаем данные там где их захотят принять
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
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

    private fun initLocation() {
        locRequest = LocationRequest.create()
        locRequest.interval = 5000
        locRequest.fastestInterval = 5000
        locRequest.priority = PRIORITY_HIGH_ACCURACY
        // Класс, который дает возможность получать сведения о местоположении
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        // Запрашиваем сведения о местоположении
        locProvider.requestLocationUpdates(
            locRequest,
            locCallBack,
            Looper.myLooper()
        )
    }

    companion object {
        const val LOC_MODEL_INTENT = "loc_intent"
        const val CHANNEL_ID = "channel_1"

        // для проверки в mainfragment сервис запущен или нет (глобальная переменная видна везде)
        var isRunning = false
        var startTimer = 0L
    }
}