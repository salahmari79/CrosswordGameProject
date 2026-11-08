package com.example.crosswordgame.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.crosswordgame.ui.theme.AccentGold
import com.example.crosswordgame.ui.theme.CardNavy
import com.example.crosswordgame.ui.theme.DarkBlueBg
import com.example.crosswordgame.utils.DropTarget

/**
 * Professional CrosswordGrid: Optimized for mobile responsiveness and visual precision.
 */
@Composable
fun CrosswordGrid(
    rows: Int,
    cols: Int,
    gridState: Map<Pair<Int, Int>, String>,
    onCellClick: (Int, Int) -> Unit,
    selectedWordCells: Set<Pair<Int, Int>>,
    allWordCells: Set<Pair<Int, Int>>
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Dynamic cell sizing based on grid dimensions
    val maxGridWidth = screenWidth - 32.dp
    val cellSize = (maxGridWidth / cols).coerceAtMost(45.dp)

    Column(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (r in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                for (c in 0 until cols) {
                    val isPartofAnyWord = allWordCells.contains(r to c)
                    
                    if (isPartofAnyWord) {
                        val letter = gridState[r to c]
                        val isPartofSelected = selectedWordCells.contains(r to c)
                        
                        DropTarget(
                            id = "$r,$c",
                            modifier = Modifier.size(cellSize)
                        ) { isHovered ->
                            GridCell(
                                letter = letter ?: "",
                                isPartofSelected = isPartofSelected,
                                isHovered = isHovered,
                                cellSize = cellSize,
                                onClick = { onCellClick(r, c) }
                            )
                        }
                    } else {
                        // Empty space (classic crossword style)
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridCell(
    letter: String,
    isPartofSelected: Boolean,
    isHovered: Boolean,
    cellSize: Dp,
    onClick: () -> Unit
) {
    // Polished success animation
    val infiniteTransition = rememberInfiniteTransition(label = "cell_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when {
                    letter.isNotEmpty() -> AccentGold
                    isHovered -> Color.Green.copy(alpha = 0.45f)
                    isPartofSelected -> CardNavy.copy(alpha = 0.98f)
                    else -> CardNavy.copy(alpha = 0.75f)
                }
            )
            .border(
                width = if (isPartofSelected) 2.dp else 1.dp,
                color = when {
                    isHovered -> Color.Green
                    isPartofSelected -> AccentGold
                    else -> Color.White.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (letter.isNotEmpty()) {
            // Visual success glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = glowAlpha))
            )
            Text(
                text = letter,
                color = DarkBlueBg,
                fontSize = (cellSize.value * 0.65).sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
