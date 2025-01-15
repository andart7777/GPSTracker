package com.example.gpstracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gpstracker.location.LocationModel

class MainViewModel: ViewModel() {
    val locationUpdates = MutableLiveData<LocationModel>()
    // Для безопасной передачи данных в textview, в случае если он еще не нарисован или fragment изменен (переворот экрана) будет ошибка
    // (в MutableLiveData добавляется специальный обсервер который следить за циклом жизни нашего фрагмента)
    var timeData = MutableLiveData<String>()
}