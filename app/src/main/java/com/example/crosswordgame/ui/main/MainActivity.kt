package com.example.crosswordgame.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.crosswordgame.R
import com.example.crosswordgame.data.local.AppDatabase
import com.example.crosswordgame.data.local.WordEntity
import com.example.crosswordgame.di.DependencyProvider
import com.example.crosswordgame.domain.model.GameLevel
import com.example.crosswordgame.domain.model.StageData
import com.example.crosswordgame.ui.game.GameScreen
import com.example.crosswordgame.ui.game.GameViewModel
import com.example.crosswordgame.utils.CrosswordGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * MainActivity: Coordinates global initialization and view orchestration.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies through ViewModel proxy
        viewModel.initialize(this)
        
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MaterialTheme {
                    LaunchedEffect(Unit) {
                        prepopulateAndBootstrap()
                    }
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * Seeds database and triggers the first stage generation.
     */
    private suspend fun prepopulateAndBootstrap() {
        withContext(Dispatchers.IO) {
            val db = DependencyProvider.getDatabase(applicationContext)
            
            if (db.wordDao().getTotalCount(GameLevel.A1) == 0) {
                loadWords(db, R.raw.stage_1, GameLevel.A1, "\t")
            }
            if (db.wordDao().getTotalCount(GameLevel.A2) == 0) {
                loadWords(db, R.raw.stage_2, GameLevel.A2, ",")
            }

            val currentLevel = viewModel.userStats.currentLevel
            val solved = db.wordDao().getSolvedCount(currentLevel)
            val total = db.wordDao().getTotalCount(currentLevel)
            
            withContext(Dispatchers.Main) {
                viewModel.updateUserStats(solved, total)
                triggerNextStageGeneration()
            }
        }
    }

    private fun triggerNextStageGeneration() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DependencyProvider.getDatabase(applicationContext)
            val level = viewModel.userStats.currentLevel
            val gridSize = if (level == GameLevel.A1) 10 else 14
            
            // Uses the new spaced-repetition aware candidate fetcher
            val candidates = db.wordDao().getCandidateWords(level, 20)
            if (candidates.isNotEmpty()) {
                val words = CrosswordGenerator(gridSize).generate(candidates)
                if (words.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        viewModel.startNewStage(StageData(1, level, gridSize, gridSize, words))
                    }
                }
            }
        }
    }

    private suspend fun loadWords(db: AppDatabase, resId: Int, level: GameLevel, sep: String) {
        val reader = BufferedReader(InputStreamReader(resources.openRawResource(resId)))
        val entities = mutableListOf<WordEntity>()
        reader.readLine() // Header

        var line: String? = reader.readLine()
        var idCounter = 1
        while (line != null) {
            val tokens = line.split(sep)
            if (tokens.size >= 5) {
                entities.add(WordEntity(
                    wordId = "${level.name}_$idCounter",
                    level = level,
                    englishWord = tokens[0].trim().uppercase(),
                    partOfSpeech = tokens[1].trim(),
                    arabicMeaning = tokens[2].trim(),
                    memoryHint = tokens[3].trim(),
                    category = tokens[4].trim()
                ))
                idCounter++
            }
            line = reader.readLine()
        }
        db.wordDao().insertWords(entities)
    }
}
