package com.example.crosswordgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crosswordgame.domain.model.WordData
import com.example.crosswordgame.ui.theme.AccentGold
import com.example.crosswordgame.ui.theme.CardNavy

/**
 * Professional ClueCard: Displays thematic hints and educational data.
 */
@Composable
fun ClueCard(
    selectedWord: WordData?,
    isLevelComplete: Boolean,
    onPronounce: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = CardNavy.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "HINT / تلميح",
                color = AccentGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            val hintText = when {
                isLevelComplete -> "Congratulations! Stage Solved"
                selectedWord != null -> selectedWord.arabicDefinition
                else -> "Select a word to reveal the clue"
            }

            Text(
                text = hintText,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            if (selectedWord != null && !isLevelComplete) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { onPronounce(selectedWord.englishWord) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Pronounce",
                        tint = AccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Pronounce", color = AccentGold, fontSize = 12.sp)
                }
            }
        }
    }
}
