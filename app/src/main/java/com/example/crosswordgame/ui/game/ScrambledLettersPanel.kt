package com.example.crosswordgame.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.crosswordgame.utils.DraggableItem

/**
 * Panel for available letters with professional drag and drop support.
 */
@Composable
fun ScrambledLettersPanel(
    letters: List<Char>,
    cellSize: Dp,
    onLetterDropped: (String?, Char) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // itemsIndexed with key ensures proper animations and state stability during drag
        itemsIndexed(letters, key = { index, letter -> "$letter-$index" }) { _, letter ->
            DraggableItem(
                data = letter,
                onDrop = onLetterDropped
            ) {
                LetterTile(letter = letter, size = cellSize)
            }
        }
    }
}
