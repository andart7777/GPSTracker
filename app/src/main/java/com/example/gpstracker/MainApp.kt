package com.example.gpstracker

import android.app.Application
import com.example.gpstracker.db.MainDb

class MainApp : Application() {
    val database by lazy { MainDb.getDataBase(this) }
}