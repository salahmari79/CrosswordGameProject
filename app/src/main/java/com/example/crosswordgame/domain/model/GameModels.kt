package com.example.crosswordgame.domain.model

enum class Direction { HORIZONTAL, VERTICAL }

enum class GameLevel(val displayName: String, val order: Int) {
    A1("Beginner", 1),
    A2("Elementary", 2),
    B1("Intermediate", 3),
    B2("Upper Intermediate", 4),
    C1("Advanced", 5);

    companion object {
        fun fromOrder(order: Int): GameLevel = entries.find { it.order == order } ?: A1
    }
}

/**
 * Enhanced WordData to support educational features like audio and thematic categories.
 */
data class WordData(
    val wordId: String,
    val englishWord: String,
    val arabicDefinition: String,
    val arabicTranslation: String,
    val startRow: Int,
    val startCol: Int,
    val direction: Direction,
    val isSolved: Boolean = false,
    val category: String = "General",
    val audioUrl: String? = null,
    val imageUrl: String? = null
)

data class StageData(
    val stageNumber: Int,
    val level: GameLevel,
    val gridRows: Int,
    val gridCols: Int,
    val words: List<WordData>
)

data class UserStats(
    val totalPoints: Int = 0,
    val currentLevel: GameLevel = GameLevel.A1,
    val solvedCount: Int = 0,
    val totalInLevel: Int = 0,
    val remainingLives: Int = 5,
    val isGuest: Boolean = true
) {
    val progress: Float
        get() = if (totalInLevel > 0) solvedCount.toFloat() / totalInLevel else 0f
}
