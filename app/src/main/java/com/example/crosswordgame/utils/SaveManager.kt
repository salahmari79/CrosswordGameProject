package com.example.crosswordgame.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.crosswordgame.config.FirebaseConfig
import com.example.crosswordgame.data.auth.GameStats
import com.example.crosswordgame.domain.model.GameLevel
import com.example.crosswordgame.domain.model.UserStats
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

/**
 * SaveManager: Handles local and remote persistence of game progress.
 * Manages the conversion between local UserStats and cloud GameStats.
 */
class SaveManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("game_persistence", Context.MODE_PRIVATE)
    private val firestore = FirebaseConfig.firestore
    private val gson = Gson()

    companion object {
        private const val KEY_USER_STATS = "user_stats_v2"
    }

    // --- Local Persistence ---

    fun saveLocally(stats: UserStats) {
        val json = gson.toJson(stats)
        prefs.edit().putString(KEY_USER_STATS, json).apply()
    }

    fun loadLocally(): UserStats? {
        val json = prefs.getString(KEY_USER_STATS, null) ?: return null
        return try {
            gson.fromJson(json, UserStats::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- Cloud Synchronization ---

    /**
     * Merges cloud data with local data, prioritizing the higher score/level.
     */
    suspend fun syncUserStats(userId: String, localStats: UserStats): UserStats {
        val cloudData = loadFromCloud(userId)
        
        if (cloudData != null) {
            val cloudStats = cloudData["gameStats"] as? Map<String, Any>
            if (cloudStats != null) {
                val cloudHighScore = (cloudStats["highScore"] as? Number)?.toInt() ?: 0
                val cloudLevelOrder = (cloudStats["level"] as? Number)?.toInt() ?: 1
                
                if (cloudHighScore > localStats.totalPoints || cloudLevelOrder > localStats.currentLevel.order) {
                    val merged = localStats.copy(
                        totalPoints = maxOf(localStats.totalPoints, cloudHighScore),
                        currentLevel = if (cloudLevelOrder > localStats.currentLevel.order) 
                            GameLevel.fromOrder(cloudLevelOrder) else localStats.currentLevel
                    )
                    saveLocally(merged)
                    return merged
                }
            }
        }
        
        // If local is fresher or cloud is empty, upload local
        saveUserStatsToCloud(userId, localStats)
        return localStats
    }

    suspend fun saveUserStatsToCloud(userId: String, stats: UserStats) {
        val cloudPayload = GameStats(
            level = stats.currentLevel.order,
            highScore = stats.totalPoints,
            puzzlesSolved = stats.solvedCount
        )
        
        try {
            firestore.collection("players")
                .document(userId)
                .set(mapOf("gameStats" to cloudPayload), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            // Log or handle failure (e.g., retry logic)
        }
    }

    private suspend fun loadFromCloud(userId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection("players").document(userId).get().await()
            doc.data
        } catch (e: Exception) {
            null
        }
    }
}
