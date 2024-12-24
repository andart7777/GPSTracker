package com.example.gpstracker.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.gpstracker.R
import com.example.gpstracker.utils.showToast

// PreferenceFragmentCompat - Специальный класс для создания окна настроек,
// используя данный класс нет необходимости сохранять значения настроек
class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference // Для получения и отображения на фронте текущего выбранной значения настройки
    private lateinit var colorPref: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    // Присваиваем в переменную timePref значения настроек
    private fun init() {
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPrefs()
    }

    // Слушатель изменений в значениях настроек
    private fun onChangeListener(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener {
        pref, value ->
            when(pref.key){
                "update_time_key" -> onTimeChange(value.toString())
                "color_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
            }
            true
        }

    }

    private fun onTimeChange(value: String) {
        // Код для отображения выбранного значения в title настроек, после выбора ( title: + 3 sec )
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title.toString().substringBefore(":")
        val pos = valueArray.indexOf(value)
        timePref.title = "$title: ${nameArray[pos]}"
    }

    // Предотваращение стирания окна при повторном открытии (с показом выбранных значений настроек)
    // Показ значений настроект на старте
    private fun initPrefs() {
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)
        val title = timePref.title
        val pos = valueArray.indexOf(pref?.getString("update_time_key", "3000"))
        timePref.title = "$title: ${nameArray[pos]}"

        val trackColor = pref?.getString("color_key", "#FF00ADFF")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }

}