package com.example.gpstracker.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpstracker.MainViewModel
import com.example.gpstracker.R
import com.example.gpstracker.databinding.FragmentMainBinding
import com.example.gpstracker.db.TrackItem
import com.example.gpstracker.location.LocationModel
import com.example.gpstracker.location.LocationService
import com.example.gpstracker.utils.DialogManager
import com.example.gpstracker.utils.TimeUtils
import com.example.gpstracker.utils.checkPermission
import com.example.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask
import kotlin.text.*

class MainFragment : Fragment() {
    private var trackItem: TrackItem? = null
    private var pl: Polyline? = null
    private var isServiceRunning = false
    private var firstStart =
        true // для заполнения списка Polyline, определение первый запуск или нет
    private var timer: Timer? = null
    private var startTime = 0L

    // С помощью pLauncher вызываем диалог. Список из разрешений которые хотим получить - Array<String>
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    private var isFirstLaunch = true // Флаг для первого запуска
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MyLog", "onCreateView")
        settingsOsm() // Инициализация карты до загрузки главного экрана
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MyLog", "onViewCreated")
        registerPermission() // Сначала регистрируем Launcher
        checkLocPermission() // Используем Launcher после регистрации иначе он выдаст null
        setOnClicks()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        locationUpdates()
    }

    private fun setOnClicks() = with(binding) {
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
    }

    // Для экономии ресурсов общий слушатель нажатий для всех кнопок
    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when (it.id) {
                R.id.fStartStop -> startStopService()
            }
        }
    }

    // MainViewModel
    private fun locationUpdates() = with(binding) {
        model.locationUpdates.observe(viewLifecycleOwner) {
            val distance = "Distance: ${String.format("%.1f", it.distance)} m"
            val velocity = "Velocity: ${String.format("%.1f", 3.6f * it.velocity)} km/h"
            val averageVelocity = "Average velocity: ${getAverageSpeed(it.distance)} km/h"
            tvDistance.text = distance
            tvVelocity.text = velocity
            tvAverageVel.text = averageVelocity
            // Запись данных в базу данных
            trackItem = TrackItem(
                null,
                getCurrentTime(),
                TimeUtils.getDate(),
                String.format("%.1f", it.distance / 1000),
                getAverageSpeed(it.distance),
                ""
            )
            updatePolyline(it.geoPointsList)
        }
    }

    private fun updateTime() {
        model.timeData.observe(viewLifecycleOwner) {
            binding.tvTime.text = it
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTimer // было System.currentTimeMillis()
        timer?.schedule(object : TimerTask() {
            override fun run() { // запускается на второстепенном потоке
                activity?.runOnUiThread { // запуск на основном потоке
                    model.timeData.value = getCurrentTime()
                }
            }

        }, 1, 1)
    }

    private fun getAverageSpeed(distance: Float): String {
        return String.format(
            "%.1f",
            3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000.0f))
        )
    }

    private fun getCurrentTime(): String {
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }

    private fun startStopService() {
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()
            DialogManager.showSaveDialog(requireContext(),
                trackItem,
                object : DialogManager.Listener {
                    override fun onClick() {
                        showToast("Track saved!!")
                    }
                })
        }
        isServiceRunning = !isServiceRunning
    }

    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }

    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTimer = System.currentTimeMillis()
        startTimer()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyLog", "onResume")
        if (!isFirstLaunch) { // Выполняем проверку только если это не первый запуск
            // (предотвращение повторного запуска checkLocPermission при старте)
            checkLocPermission()
        } else {
            isFirstLaunch = false // Сбрасываем флаг после первого запуска
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("MyLog", "onPause")

    }

    // Базовые настройки карты OSM
    private fun settingsOsm() {
        Log.d("MyLog", "settingsOsm")
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    // Инициализация карты
    private fun initOsm() = with(binding) {
        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLUE
        Log.d("MyLog", "initOsm")
        map.controller.setZoom(18.0)
//        map.controller.animateTo(GeoPoint(42.87382619104484, 74.59014113895424)) // Бишкек
        // GpsMyLocationProvider - выдает местоположение
        val mLocProvider = GpsMyLocationProvider(activity)
        // Overlay - слой наложения точек, меток на карте и получение местоположения
        // (точка с текущим местоположением)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        // Включить определение местоположения устройства
        mLocOverlay.enableMyLocation()
        // Перемещение карты, слежение за перемещением устройства
        // При использовании зум на карте, слежение отключается, необходима доп кнопка интерфейса,
        // для возврата к слежению (центр карты)
        mLocOverlay.enableFollowLocation()
        // Запуск слоев Overlay, после отработки gps на устройстве (gps отрабатывает с задержкой)
        mLocOverlay.runOnFirstFix {
            // Очищаем экран при первом запуске
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
            map.overlays.add(pl)
        }
    }

    // Регистрация и запуск запроса разрешений
    private fun registerPermission() {
        Log.d("MyLog", "registerPermission")
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { resultPerm ->
            for ((permission, granted) in resultPerm) {
                Log.d("MyLog", "Permission: $permission, Granted: $granted")
            }
            handlePermissionResult(resultPerm)
        }
    }

    // Обрабатываем результат разрешения
    private fun handlePermissionResult(resultPerm: Map<String, Boolean>) {
        Log.d("MyLog", "handlePermissionResult")
        // Проверяем разрешение FINE_LOCATION
        if (resultPerm[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Если Android >= 10, запрашиваем BACKGROUND_LOCATION
                if (!checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    requestBackgroundLocationPermission()
                } else {
                    initOsm() // Инициализация карты, если оба разрешения уже есть
                    checkLocationEnabled() // Проверяем включен-ли GPS на устройстве
                }
            } else {
                initOsm() // Если Android < 10, достаточно только FINE_LOCATION
                checkLocationEnabled() // Проверяем включен-ли GPS на устройстве
            }
        } else {
            showToast("Вы не дали разрешение на использование местоположения!")
        }
    }

    // Запрос FINE_LOCATION
    private fun requestFineLocationPermission() {
        Log.d("MyLog", "requestFineLocationPermission")
        pLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    // Запрос BACKGROUND_LOCATION
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        Log.d("MyLog", "requestBackgroundLocationPermission")
        pLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        )
    }

    // Проверка версии Android, запуск соответствующей функции проверки разрешений (ниже и выше 10 версии)
    private fun checkLocPermission() {
        Log.d("MyLog", "checkLocPermission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    // Проверка разрешений для версии Android >= 10, если нет запуск диалога Android разрешение на GPS
    @RequiresApi(Build.VERSION_CODES.Q) // Аннотация сообщает Котлину, функция только для версии Android >= 10
    private fun checkPermissionAfter10() {
        Log.d("MyLog", "checkPermissionAfter10")
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                initOsm() // Если оба разрешения уже есть
                checkLocationEnabled() // Проверяем включен-ли GPS на устройстве
            } else {
                requestBackgroundLocationPermission() // Запрашиваем только BACKGROUND_LOCATION
            }
        } else {
            requestFineLocationPermission() // Сначала запрашиваем FINE_LOCATION
        }
    }

    // Проверка разрешений для Android < 10
    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOsm()
            checkLocationEnabled() // Проверяем включен-ли GPS на устройстве
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkLocationEnabled() {
        Log.d("MyLog", "checkLocationEnabled")
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnable = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnable) {
            DialogManager.showLocEnableDialog(
                activity as AppCompatActivity,
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            )
            showToast("GPS Off - Выключен")
        } else {
            showToast("GPS On - Включен")
        }
    }

    // Получаем данные из Intent из locData
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Фильтруем по нашему Intent
            if (intent?.action == LocationService.LOC_MODEL_INTENT) {
                val locModel =
                    intent.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                Log.d("MylogReceiver", "Distance: ${locModel.distance}")
                model.locationUpdates.value = locModel
            }
        }
    }

    // Регистрируем Receiver
    private fun registerLocReceiver() {
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, locFilter)
    }

    // Добавление точек онлайн
    private fun addPoint(list: List<GeoPoint>) {
        pl?.addPoint(list[list.size - 1])
    }

    // Заполнение точек после возврата в приложение из фонового сервиса
    private fun fillPolyline(list: List<GeoPoint>) {
        list.forEach {
            pl?.addPoint(it)
        }
    }

    // Проверяем первый запуск - перегружаем список полностью, иначе добавляем по одной точке.
    private fun updatePolyline(list: List<GeoPoint>) {
        // проверяем если список геоточек не пустой
        if (list.size > 1 && firstStart) {
            fillPolyline(list)
            firstStart = false
        } else {
            addPoint(list)
        }
    }

    // Закрываем регистрацию на получение данных Broadcast, как только фрагмент закрывается
    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}