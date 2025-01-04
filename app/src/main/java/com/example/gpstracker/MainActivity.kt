package com.example.gpstracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gpstracker.databinding.ActivityMainBinding
import com.example.gpstracker.fragments.MainFragment
import com.example.gpstracker.fragments.SettingsFragment
import com.example.gpstracker.fragments.TracksFragment
import com.example.gpstracker.utils.openFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBottomNavClick()
        openFragment(MainFragment.newInstance())

    }

    private fun onBottomNavClick() {
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.id_home -> openFragment(MainFragment.newInstance())
                R.id.id_tracks -> openFragment(TracksFragment.newInstance())
                R.id.id_settings -> openFragment(SettingsFragment())
            }
            true
        }

    }
}
