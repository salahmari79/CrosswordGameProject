package com.example.crosswordgame.data.auth

data class PlayerProfile(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val provider: String = "email",
    val gameStats: GameStats = GameStats(),
    val settings: PlayerSettings = PlayerSettings()
)

data class GameStats(
    val level: Int = 1,
    val experience: Int = 0,
    val experienceToNextLevel: Int = 100,
    val coins: Int = 0,
    val gems: Int = 0,
    val puzzlesSolved: Int = 0,
    val totalPlayTime: Long = 0,
    val highScore: Int = 0
)

data class PlayerSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val language: String = "ar"
)