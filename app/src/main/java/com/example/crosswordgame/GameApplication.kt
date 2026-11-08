// 📁 app/src/main/java/com/example/crosswordgame/GameApplication.kt
package com.example.crosswordgame

import android.app.Application
import com.example.crosswordgame.config.FirebaseConfig

class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseConfig.init()
    }
}