package com.example.crosswordgame.core.manager

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core Game Engine responsible for the Game Loop and state transitions.
 */
class GameEngine {
    private val _gameTime = MutableStateFlow(0L)
    val gameTime = _gameTime.asStateFlow()

    private var gameLoopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var isPaused = false

    fun startLoop() {
        if (gameLoopJob != null) return
        
        gameLoopJob = scope.launch {
            while (isActive) {
                if (!isPaused) {
                    _gameTime.value += 1
                    // Perform periodic checks (e.g., performance, autosave)
                    if (_gameTime.value % 60 == 0L) { // Every minute
                         // Trigger autosave via SaveManager
                    }
                }
                delay(1000) // 1 second tick for a crossword game is enough
            }
        }
    }

    fun pause() { isPaused = true }
    fun resume() { isPaused = false }
    
    fun stopLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }
}
