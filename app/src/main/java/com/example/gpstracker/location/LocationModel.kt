package com.example.gpstracker.location

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>

): Serializable
// Serializable - для передачи в intent (не можем отправлять данный data class из не скольких элементов, только простые данные int, string)
// Serializable - разбирает данные по байтам передает и собирает обратно
