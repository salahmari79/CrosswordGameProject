package com.example.crosswordgame.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Intent
import com.example.crosswordgame.domain.model.Direction
import com.example.crosswordgame.ui.components.CrosswordGrid
import com.example.crosswordgame.ui.theme.AccentGold
import com.example.crosswordgame.ui.theme.DarkBlueBg
import com.example.crosswordgame.ui.theme.CardNavy
import com.example.crosswordgame.utils.*
import com.example.crosswordgame.core.manager.Achievement
import com.example.crosswordgame.ui.components.ClueCard
import com.example.crosswordgame.ui.profile.ProfileActivity

/**
 * GameScreen: The primary production-grade gameplay interface.
 * Implements low-latency animations and high-performance rendering logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val stage = viewModel.currentStage
    val selectedWord = viewModel.selectedWord
    val stats = viewModel.userStats
    val scrambled = viewModel.scrambledLetters
    val isComplete = viewModel.isLevelComplete
    val isLoading = viewModel.isLoading
    
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Performance Optimization: Derived state for word masks
    val allWordCells = remember(stage) {
        stage?.words?.flatMap { word ->
            word.englishWord.indices.map { i ->
                val r = if (word.direction == Direction.HORIZONTAL) word.startRow else word.startRow + i
                val c = if (word.direction == Direction.HORIZONTAL) word.startCol + i else word.startCol
                r to c
            }
        }?.toSet() ?: emptySet()
    }

    val selectedCells = remember(selectedWord) {
        selectedWord?.let { word ->
            word.englishWord.indices.map { i ->
                val r = if (word.direction == Direction.HORIZONTAL) word.startRow else word.startRow + i
                val c = if (word.direction == Direction.HORIZONTAL) word.startCol + i else word.startCol
                r to c
            }.toSet()
        } ?: emptySet()
    }

    DragDropContainer(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "English Crossword",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Help/Manual */ }) {
                            Icon(Icons.Default.Info, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        }) {
                            Icon(Icons.Default.AccountCircle, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlueBg)
                )
            },
            containerColor = DarkBlueBg
        ) { padding ->
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGold)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Level & Score
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Level", color = Color.Gray, fontSize = 10.sp)
                            Text(stats.currentLevel.displayName, color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        
                        // Dynamic Progress Circle
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = stats.progress,
                                color = AccentGold,
                                trackColor = CardNavy,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(52.dp)
                            )
                            Text("${(stats.progress * 100).toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Score", color = Color.Gray, fontSize = 10.sp)
                            Text("${stats.totalPoints}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Educational Clue
                    ClueCard(
                        selectedWord = selectedWord,
                        isLevelComplete = isComplete,
                        onPronounce = { /* Triggered automatically in VM on word selection */ }
                    )

                    // The Interactive Grid
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (stage != null) {
                            CrosswordGrid(
                                rows = stage.gridRows,
                                cols = stage.gridCols,
                                gridState = viewModel.gridState,
                                onCellClick = viewModel::onCellClick,
                                selectedWordCells = selectedCells,
                                allWordCells = allWordCells
                            )
                        }
                    }

                    // Tray Panel
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(170.dp),
                        color = CardNavy,
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isComplete) {
                                Button(
                                    onClick = { viewModel.requestNextStage() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(0.7f).height(56.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, null, tint = DarkBlueBg)
                                    Spacer(Modifier.width(12.dp))
                                    Text("PLAY NEXT STAGE", color = DarkBlueBg, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                }
                            } else {
                                Text(
                                    "DRAG LETTERS TO FILL THE GRID", 
                                    color = Color.White.copy(alpha = 0.5f), 
                                    fontSize = 11.sp,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                val cols = stage?.gridCols ?: 10
                                val cellSize = ((screenWidth - 48.dp) / cols).coerceAtMost(45.dp)

                                ScrambledLettersPanel(
                                    letters = scrambled,
                                    cellSize = cellSize,
                                    onLetterDropped = { targetId, letter ->
                                        targetId?.split(",")?.let {
                                            viewModel.onLetterDropped(it[0].toInt(), it[1].toInt(), letter)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LetterTile(letter: Char, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(AccentGold)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.uppercaseChar().toString(),
            color = DarkBlueBg,
            fontSize = (size.value * 0.55).sp,
            fontWeight = FontWeight.Black
        )
    }
}
