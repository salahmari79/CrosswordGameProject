package com.example.crosswordgame.ui.game

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crosswordgame.data.auth.AuthManager
import com.example.crosswordgame.data.local.AppDatabase
import com.example.crosswordgame.domain.model.*
import com.example.crosswordgame.utils.GameAudioManager
import com.example.crosswordgame.core.manager.*
import com.example.crosswordgame.data.auth.PlayerProfile
import com.example.crosswordgame.di.DependencyProvider
import com.example.crosswordgame.utils.AppLogger
import com.example.crosswordgame.utils.SaveManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GameViewModel: Architected for Production Quality.
 * Implements MVVM with strict separation of concerns and optimized reactive state.
 */
class GameViewModel : ViewModel() {

    // --- Observable UI State (Single Source of Truth) ---
    var userStats by mutableStateOf(UserStats())
        private set

    var currentStage by mutableStateOf<StageData?>(null)
        private set

    var selectedWord by mutableStateOf<WordData?>(null)
        private set

    val wordsState = mutableStateListOf<WordData>()
    val gridState = mutableStateMapOf<Pair<Int, Int>, String>()
    val scrambledLetters = mutableStateListOf<Char>()

    var isLevelComplete by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // --- Injected Services ---
    private var audioManager: GameAudioManager? = null
    private var db: AppDatabase? = null
    private var saveManager: SaveManager? = null
    var authManager: AuthManager? = null
        private set

    val engine = GameEngine()
    val achievementManager = AchievementManager()

    init {
        engine.startLoop()
    }

    /**
     * Bootstraps the ViewModel with required system context and dependencies.
     */
    fun initialize(context: Context) {
        if (db != null) return
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val database = DependencyProvider.getDatabase(context)
                val saver = DependencyProvider.getSaveManager(context)
                
                withContext(Dispatchers.Main) {
                    db = database
                    saveManager = saver
                    audioManager = GameAudioManager(context)
                    if (context is Activity) authManager = AuthManager(context)

                    val loadedStats = saver.loadLocally() ?: UserStats()
                    userStats = loadedStats
                    achievementManager.checkAchievements(loadedStats)
                    isLoading = false
                }
            } catch (e: Exception) {
                AppLogger.e("ViewModel", "Initialization crash avoided", e)
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    // --- Game Logic & Engine Transitions ---

    fun startNewStage(stage: StageData) {
        isLevelComplete = false
        currentStage = stage
        wordsState.clear()
        wordsState.addAll(stage.words)
        
        gridState.clear()
        stage.words.forEach { word ->
            for (i in word.englishWord.indices) {
                val r = if (word.direction == Direction.HORIZONTAL) word.startRow else word.startRow + i
                val c = if (word.direction == Direction.HORIZONTAL) word.startCol + i else word.startCol
                gridState[r to c] = if (word.isSolved) word.englishWord[i].uppercase() else ""
            }
        }
        autoSelectNextWord()
    }

    fun onCellClick(row: Int, col: Int) {
        val word = wordsState.find { w ->
            val rRange = if (w.direction == Direction.VERTICAL) w.startRow until w.startRow + w.englishWord.length else w.startRow..w.startRow
            val cRange = if (w.direction == Direction.HORIZONTAL) w.startCol until w.startCol + w.englishWord.length else w.startCol..w.startCol
            row in rRange && col in cRange
        }
        if (word != null) { 
            selectedWord = word
            refreshScrambled(word)
            audioManager?.playPronunciation(word.englishWord)
        }
    }

    private fun autoSelectNextWord() {
        val next = wordsState.find { !it.isSolved }
        selectedWord = next
        refreshScrambled(next)
    }

    private fun refreshScrambled(word: WordData?) {
        scrambledLetters.clear()
        if (word != null && !word.isSolved) {
            val missing = mutableListOf<Char>()
            for (i in word.englishWord.indices) {
                val r = if (word.direction == Direction.HORIZONTAL) word.startRow else word.startRow + i
                val c = if (word.direction == Direction.HORIZONTAL) word.startCol + i else word.startCol
                if (gridState[r to c].isNullOrEmpty()) missing.add(word.englishWord[i].uppercaseChar())
            }
            scrambledLetters.addAll(missing.shuffled())
        }
    }

    fun onLetterDropped(row: Int, col: Int, letter: Char) {
        if (isLevelComplete) return
        
        val intersecting = wordsState.filter { w ->
            val idx = getIndexInWord(w, row, col)
            idx != -1
        }
        
        val isCorrect = intersecting.any { w ->
            val idx = getIndexInWord(w, row, col)
            w.englishWord[idx].uppercaseChar() == letter.uppercaseChar()
        }

        if (isCorrect) {
            if (gridState[row to col].isNullOrEmpty()) {
                gridState[row to col] = letter.uppercaseChar().toString()
                audioManager?.playSuccess()
                scrambledLetters.remove(letter)
                intersecting.forEach { checkIfWordSolved(it) }
            }
        } else {
            audioManager?.playError()
            // Track error for spaced repetition
            intersecting.forEach { viewModelScope.launch(Dispatchers.IO) { db?.wordDao()?.incrementError(it.wordId, System.currentTimeMillis()) } }
            userStats = userStats.copy(remainingLives = (userStats.remainingLives - 1).coerceAtLeast(0))
        }
    }

    private fun getIndexInWord(w: WordData, r: Int, c: Int): Int {
        return if (w.direction == Direction.HORIZONTAL) {
            if (r == w.startRow && c >= w.startCol && c < w.startCol + w.englishWord.length) c - w.startCol else -1
        } else {
            if (c == w.startCol && r >= w.startRow && r < w.startRow + w.englishWord.length) r - w.startRow else -1
        }
    }

    private fun checkIfWordSolved(word: WordData) {
        val solved = word.englishWord.indices.all { i ->
            val r = if (word.direction == Direction.HORIZONTAL) word.startRow else word.startRow + i
            val c = if (word.direction == Direction.HORIZONTAL) word.startCol + i else word.startCol
            gridState[r to c]?.isNotEmpty() == true
        }
        if (solved) finalizeWordSolution(word)
    }

    private fun finalizeWordSolution(word: WordData) {
        val idx = wordsState.indexOfFirst { it.wordId == word.wordId }
        if (idx != -1 && !wordsState[idx].isSolved) {
            wordsState[idx] = wordsState[idx].copy(isSolved = true)
            updateDatabaseAndProgress(word.wordId)
            
            if (wordsState.all { it.isSolved }) {
                isLevelComplete = true
                scrambledLetters.clear()
            } else if (selectedWord?.wordId == word.wordId) {
                autoSelectNextWord()
            }
        }
    }

    private fun updateDatabaseAndProgress(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.wordDao()?.markAsSolved(id)
            val solved = db?.wordDao()?.getSolvedCount(userStats.currentLevel) ?: 0
            val total = db?.wordDao()?.getTotalCount(userStats.currentLevel) ?: 0
            
            withContext(Dispatchers.Main) {
                userStats = userStats.copy(
                    solvedCount = solved, 
                    totalInLevel = total, 
                    totalPoints = userStats.totalPoints + 10
                )
                saveManager?.saveLocally(userStats)
                syncCloudProgress()
                achievementManager.checkAchievements(userStats)
                if (userStats.progress >= 0.99f) levelUp()
            }
        }
    }

    private fun levelUp() {
        userStats = userStats.copy(
            currentLevel = GameLevel.fromOrder(userStats.currentLevel.order + 1),
            solvedCount = 0,
            totalInLevel = 0
        )
    }

    // --- Authentication & Cloud Persistence ---

    fun registerWithEmail(email: String, pass: String, user: String, onResult: (Result<FirebaseUser>) -> Unit) {
        viewModelScope.launch {
            authManager?.registerWithEmail(email, pass, user)?.let { result ->
                if (result.isSuccess) result.getOrNull()?.let { u -> saveManager?.saveUserStatsToCloud(u.uid, userStats) }
                onResult(result)
            }
        }
    }

    fun skipLogin() { userStats = userStats.copy(isGuest = true) }

    fun signInWithEmail(email: String, pass: String, onResult: (Result<FirebaseUser>) -> Unit) {
        viewModelScope.launch {
            authManager?.signInWithEmail(email, pass)?.let { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let { user -> userStats = saveManager?.syncUserStats(user.uid, userStats) ?: userStats }
                }
                onResult(result)
            }
        }
    }

    fun signInWithFacebook(token: String, onResult: (Result<FirebaseUser>) -> Unit) {
        viewModelScope.launch {
            authManager?.signInWithFacebook(token)?.let { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let { user -> userStats = saveManager?.syncUserStats(user.uid, userStats) ?: userStats }
                }
                onResult(result)
            }
        }
    }

    private fun syncCloudProgress() {
        authManager?.getCurrentUser()?.let { user ->
            viewModelScope.launch(Dispatchers.IO) { saveManager?.saveUserStatsToCloud(user.uid, userStats) }
        }
    }

    fun updateUserStats(solved: Int, total: Int) {
        userStats = userStats.copy(solvedCount = solved, totalInLevel = total)
    }

    fun requestNextStage() { isLevelComplete = false }

    suspend fun loadPlayerProfile(): Result<PlayerProfile> = authManager?.loadPlayerProfile() ?: Result.failure(Exception("Not logged in"))
    suspend fun resetPassword(email: String): Result<Unit> = authManager?.resetPassword(email) ?: Result.failure(Exception("Not logged in"))

    override fun onCleared() {
        super.onCleared()
        engine.stopLoop()
        audioManager?.release()
    }
}
