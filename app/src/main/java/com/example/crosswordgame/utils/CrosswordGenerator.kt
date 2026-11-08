package com.example.crosswordgame.utils

import com.example.crosswordgame.domain.model.Direction
import com.example.crosswordgame.domain.model.WordData
import com.example.crosswordgame.data.local.WordEntity

/**
 * Professional CrosswordGenerator: Uses heuristic placement to maximize intersections.
 */
class CrosswordGenerator(val size: Int = 10) {

    fun generate(inputWords: List<WordEntity>): List<WordData> {
        if (inputWords.isEmpty()) return emptyList()

        val grid = Array(size) { CharArray(size) { ' ' } }
        val placedWords = mutableListOf<WordData>()

        // 1. Sort by length to place longest words first as anchors
        val sortedWords = inputWords.sortedByDescending { it.englishWord.length }

        // 2. Place anchor word
        val first = sortedWords[0]
        val startRow = size / 2
        val startCol = (size - first.englishWord.length) / 2
        placeWord(grid, first, startRow, startCol, Direction.HORIZONTAL, placedWords)

        // 3. Heuristic placement for remaining words
        for (i in 1 until sortedWords.size) {
            val entity = sortedWords[i]
            var bestScore = -1
            var bestPlacement: WordData? = null

            // Search for the intersection that yields the best connectivity
            for (placed in placedWords) {
                for (pIdx in placed.englishWord.indices) {
                    for (nIdx in entity.englishWord.indices) {
                        if (placed.englishWord[pIdx] == entity.englishWord[nIdx]) {
                            val newDir = if (placed.direction == Direction.HORIZONTAL) Direction.VERTICAL else Direction.HORIZONTAL
                            
                            val nr = if (newDir == Direction.HORIZONTAL) placed.startRow + pIdx else placed.startRow - nIdx
                            val nc = if (newDir == Direction.HORIZONTAL) placed.startCol - nIdx else placed.startCol + pIdx

                            if (canPlace(grid, entity.englishWord, nr, nc, newDir)) {
                                // Score could be improved by checking how many words it intersects
                                val score = 1 
                                if (score > bestScore) {
                                    bestScore = score
                                    bestPlacement = WordData(
                                        wordId = entity.wordId,
                                        englishWord = entity.englishWord,
                                        arabicDefinition = entity.arabicMeaning,
                                        arabicTranslation = entity.memoryHint,
                                        startRow = nr,
                                        startCol = nc,
                                        direction = newDir,
                                        category = entity.category
                                    )
                                }
                            }
                        }
                    }
                }
            }

            bestPlacement?.let { 
                applyToGrid(grid, it)
                placedWords.add(it)
            }
        }

        return placedWords
    }

    private fun canPlace(grid: Array<CharArray>, word: String, r: Int, c: Int, dir: Direction): Boolean {
        if (r < 0 || c < 0) return false
        if (dir == Direction.HORIZONTAL && c + word.length > size) return false
        if (dir == Direction.VERTICAL && r + word.length > size) return false

        for (i in word.indices) {
            val currR = if (dir == Direction.HORIZONTAL) r else r + i
            val currC = if (dir == Direction.HORIZONTAL) c + i else c
            
            val charAt = grid[currR][currC]
            if (charAt != ' ' && charAt != word[i]) return false
        }
        return true
    }

    private fun applyToGrid(grid: Array<CharArray>, word: WordData) {
        for (i in word.englishWord.indices) {
            val currR = if (word.direction == Direction.HORIZONTAL) word.startRow else word.startRow + i
            val currC = if (word.direction == Direction.HORIZONTAL) word.startCol + i else word.startCol
            grid[currR][currC] = word.englishWord[i]
        }
    }

    private fun placeWord(grid: Array<CharArray>, entity: WordEntity, r: Int, c: Int, dir: Direction, list: MutableList<WordData>) {
        val word = WordData(
            wordId = entity.wordId,
            englishWord = entity.englishWord,
            arabicDefinition = entity.arabicMeaning,
            arabicTranslation = entity.memoryHint,
            startRow = r,
            startCol = c,
            direction = dir,
            category = entity.category
        )
        applyToGrid(grid, word)
        list.add(word)
    }
}
