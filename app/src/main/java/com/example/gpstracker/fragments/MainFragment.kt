package com.example.gpstracker.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gpstracker.databinding.FragmentMainBinding
import com.example.gpstracker.utils.checkPermission
import com.example.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {
    // С помощью pLauncher вызываем диалог. Список из разрешений которые хотим получить - Array<String>
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm() // Инициализация карты до загрузки главного экрана
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermission() // Сначала регистрируем Launcher
        checkLocPermission() // Используем Launcher после регистрации иначе он выдаст null
//        activity?.startService(Intent(activity, LocationService::class.java))
    }
//
//    override fun onResume() {
//        super.onResume()
//        checkLocPermission()
//    }
//
//    override fun onPause() {
//        super.onPause()
//    }

    // Базовые настройки карты OSM
    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    // Инициализация карты
    private fun initOsm() = with(binding) {
        map.controller.setZoom(10.0)
//        map.controller.animateTo(GeoPoint(42.87382619104484, 74.59014113895424)) // Бишкек
        // GpsMyLocationProvider - выдает местоположение
        val mLocProvider = GpsMyLocationProvider(activity)
        // Overlay - слой наложения точек, меток на карте и получение местоположения
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
        }
    }

    // Регистрация и запуск запроса разрешений
    private fun registerPermission() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { resultPerm ->
            for ((permission, granted) in resultPerm) {
                Log.d("TestPermission", "Permission: $permission, Granted: $granted")
            }
            // Результат запроса передается в виде словаря Map
            // if не может работать с null поэтому true
            if (resultPerm[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initOsm()
//                checkLocationEnabled()
            } else {
                showToast("Вы не дали разрешение на использование местоположения!")
            }
        }
    }

    // Проверка версии Android, запуск соответствующей функции проверки разрешений (ниже и выше 10 версии)
    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    // Проверка разрешений для версии Android >= 10, если нет запуск диалога Android разрешение на GPS
    @RequiresApi(Build.VERSION_CODES.Q) // Аннотация сообщает Котлину, функция только для версии Android >= 10
    private fun checkPermissionAfter10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOsm()
//            checkLocationEnabled()
        } else {
            pLauncher.launch( // Запуск диалога Android, разрешение на gps
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    // Выше API 30 нельзя запросить (background) одновременно с разрешением на точное местоположение (fine)
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION

                )
            )
        }
    }

    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            initOsm()
//            checkLocationEnabled()
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

//    private fun checkLocationEnabled() {
//        val LManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val isEnable = LManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//        if (!isEnable) {
//            DialogManager.showLocEnableDialog(
//                activity as AppCompatActivity,
//                object : DialogManager.Listener {
//                    override fun onClick() {
//                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                    }
//                }
//            )
//        } else {
//            showToast("GPS включен")
//        }
//    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}