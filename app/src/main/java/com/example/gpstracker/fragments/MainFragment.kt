package com.example.gpstracker.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import com.example.gpstracker.databinding.FragmentMainBinding
//import com.example.gpstracker.location.LocationService
//import com.example.gpstracker.utils.DialogManager
//import com.example.gpstracker.utils.checkPermission
import com.example.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        Log.d("MyLog", "onCreateView")
        settingsOsm() // Инициализация карты до загрузки главного экрана
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOsm()
//        Log.d("MyLog", "onViewCreated")
//        registerPermission()
//        activity?.startService(Intent(activity, LocationService::class.java))
    }

    override fun onResume() {
        super.onResume()
//        Log.d("MyLog", "onResume")
//        checkLocPermission()
    }

    override fun onPause() {
        super.onPause()
//        Log.d("MyLog", "onPause")
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding) {
        map.controller.setZoom(20.0)
        map.controller.animateTo(GeoPoint(42.87382619104484, 74.59014113895424)) // Бишкек
//        val mLocProvider = GpsMyLocationProvider(activity)
//        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
//        mLocOverlay.enableMyLocation()
//        mLocOverlay.enableFollowLocation()
//        mLocOverlay.runOnFirstFix {
//            map.overlays.clear()
//            map.overlays.add(mLocOverlay)
//        }
    }

//    private fun registerPermission() {
//        pLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        ) {
//            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
//                initOsm()
//                checkLocationEnabled()
//            }
//            //            else {
////                showToast("Вы не дали разрешение на использование местоположения!")
////            }
//        }
//    }

//    private fun checkLocPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            checkPermissionAfter10()
//        } else {
//            checkPermissionBefore10()
//        }
//    }

//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun checkPermissionAfter10() {
//        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//            && checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//        ) {
//            initOsm()
//            checkLocationEnabled()
//        } else {
//            pLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                )
//            )
//        }
//    }

//    private fun checkPermissionBefore10() {
//        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//        ) {
//            initOsm()
//            checkLocationEnabled()
//        } else {
//            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
//        }
//    }

//    private fun checkLocationEnabled() {
//        val LManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val isEnable = LManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//        if (!isEnable) {
//            DialogManager.showLocEnableDialog(
//                activity as AppCompatActivity,
//                object: DialogManager.Listener{
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