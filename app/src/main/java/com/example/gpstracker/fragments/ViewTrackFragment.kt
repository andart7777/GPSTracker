package com.example.gpstracker.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.gpstracker.MainApp
import com.example.gpstracker.MainViewModel
import com.example.gpstracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

class ViewTrackFragment : Fragment() {
    private lateinit var binding: ViewTrackBinding
    private val model: MainViewModel by activityViewModels {
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = ViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTrack()
    }

    private fun getTrack() = with(binding) {
        model.currentTrack.observe(viewLifecycleOwner) {
            val date = "Date: ${it.date}"
            val speed = "Average speed: ${it.speed}"
            val distance = "Distance: ${it.distance}"
            tvDateViewTack.text = date
            tvTime.text = it.time
            tvAverageVel.text = speed
            tvDistance.text = distance
            val polyline = getPolyline(it.geoPoints)
            map.overlays.add(polyline)
            // Добавляем BoundingBox для показа всего трека на одном экране
            Log.d("MapDebug", "Polyline points: ${polyline.actualPoints}")
            if (polyline.actualPoints.isNotEmpty()) {
                val boundingBox = BoundingBox.fromGeoPoints(polyline.actualPoints)
                map.postDelayed({
                    map.zoomToBoundingBox(boundingBox, false, 100)
                }, 500)
            } else {
                goToStartPosition(GeoPoint(0.0, 0.0)) // Если точек нет, центрируем на 0,0
            }
        }
    }

    private fun goToStartPosition(startPosition: GeoPoint) {
        binding.map.controller.zoomTo(18.0)
        binding.map.controller.animateTo(startPosition)
    }

    private fun getPolyline(geoPoint: String): Polyline {
        val polyline = Polyline()
        val list = geoPoint.split("/")
        list.forEach {
            if (it.isEmpty()) return@forEach
            val points = it.split(",")
            polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
        }
        return polyline
    }

    private fun settingsOsm() {
        Log.d("MyLog", "settingsOsm")
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewTrackFragment()
    }
}