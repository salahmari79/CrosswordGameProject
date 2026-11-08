package com.example.crosswordgame.di

import android.content.Context
import androidx.room.Room
import com.example.crosswordgame.data.local.AppDatabase
import com.example.crosswordgame.utils.SaveManager

/**
 * DependencyProvider: A simplified Service Locator for managing singleton dependencies.
 * In a larger project, this would be replaced by Hilt or Koin.
 */
object DependencyProvider {
    private var database: AppDatabase? = null
    private var saveManager: SaveManager? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "crossword_pro_db"
            ).fallbackToDestructiveMigration().build()
            database = instance
            instance
        }
    }

    fun getSaveManager(context: Context): SaveManager {
        return saveManager ?: synchronized(this) {
            val instance = SaveManager(context.applicationContext)
            saveManager = instance
            instance
        }
    }
}
