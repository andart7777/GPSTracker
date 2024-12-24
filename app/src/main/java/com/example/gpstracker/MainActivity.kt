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

//                // Test switch tabs
//                R.id.id_home -> showToast("Home") // Using extensions functions
//                R.id.id_tracks -> Toast.makeText(this, "track", Toast.LENGTH_SHORT).show()
//                R.id.id_settings -> Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show()
            }
            true
        }

    }
}