package com.example.gpstracker.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gpstracker.databinding.StepCounterWithCardBinding
import com.example.gpstracker.stepcounter.StepCounterService
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class StepCounterFragment : Fragment() {
    private lateinit var binding: StepCounterWithCardBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StepCounterWithCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().startService(Intent(requireContext(), StepCounterService::class.java))

        // Проверяем разрешение на распознавание активности
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 1001)
        } else {
            startStepService()
            setupChart()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Регистрируем ресивер
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(stepReceiver, IntentFilter("STEP_UPDATE"))
    }

    private val stepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val steps = intent?.getIntExtra("step_count", 0) ?: 0
            val stepGoal = 100 // Плановое количество шагов

            binding.tvStepCount.text = "Всего: $steps шагов"
            binding.tvStepGoal.text = "цель $stepGoal шагов"
            binding.tvStepCountToGoal.text = "${stepGoal - steps} шагов"
            // Устанавливаем прогресс
            binding.circularProgressBar.setProgressCompat(steps, true)
        }
    }


    private fun setupChart() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val entries = mutableListOf<BarEntry>()
        val random = java.util.Random()

        for ((index, day) in days.withIndex()) {
            val steps = random.nextInt(900) + 100 // Генерируем случайное число от 100 до 1000
            entries.add(BarEntry(index.toFloat(), steps.toFloat()))
        }

        val barDataSet = BarDataSet(entries, "Шаги за неделю")
        barDataSet.color = Color.parseColor("#219BCC")
        barDataSet.valueTextSize = 14f

        val barData = BarData(barDataSet)
        binding.barChart.data = barData
        binding.barChart.invalidate()
    }


    private fun startStepService() {
        requireContext().startService(Intent(requireContext(), StepCounterService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(stepReceiver)
    }

}
