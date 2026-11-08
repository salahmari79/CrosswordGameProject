package com.example.crosswordgame.core.manager

import com.example.crosswordgame.domain.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val targetValue: Int,
    val currentValue: Int = 0
)

class AchievementManager {
    private val _achievements = MutableStateFlow<List<Achievement>>(
        listOf(
            Achievement("first_word", "First Step", "Solve your first word!", targetValue = 1),
            Achievement("level_master", "Level Master", "Complete level A1", targetValue = 100),
            Achievement("point_collector", "Point Collector", "Earn 500 points", targetValue = 500)
        )
    )
    val achievements = _achievements.asStateFlow()

    fun checkAchievements(stats: UserStats) {
        val updatedList = _achievements.value.map { achievement ->
            val progress = when(achievement.id) {
                "first_word" -> stats.solvedCount
                "level_master" -> if (stats.currentLevel.order > 1) 100 else stats.solvedCount
                "point_collector" -> stats.totalPoints
                else -> achievement.currentValue
            }
            
            achievement.copy(
                currentValue = progress,
                isUnlocked = progress >= achievement.targetValue
            )
        }
        _achievements.value = updatedList
    }
}
