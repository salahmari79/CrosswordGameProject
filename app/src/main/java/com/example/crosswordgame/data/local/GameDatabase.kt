package com.example.crosswordgame.data.local

import androidx.room.*
import com.example.crosswordgame.domain.model.Direction
import com.example.crosswordgame.domain.model.GameLevel

/**
 * WordEntity: Represents the schema for the crossword words.
 * Includes support for educational categories and spaced repetition tracking.
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val wordId: String,
    val level: GameLevel,
    val englishWord: String,
    val partOfSpeech: String,
    val arabicMeaning: String,
    val memoryHint: String,
    val category: String,
    val isSolved: Boolean = false,
    val lastAttemptTimestamp: Long = 0L,
    val errorCount: Int = 0,
    // Spatial positioning (managed by CrosswordGenerator)
    val startRow: Int = 0,
    val startCol: Int = 0,
    val direction: Direction = Direction.HORIZONTAL
)

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE level = :level AND isSolved = 0 ORDER BY errorCount DESC, RANDOM() LIMIT :limit")
    fun getCandidateWords(level: GameLevel, limit: Int): List<WordEntity>

    @Query("UPDATE words SET isSolved = 1, errorCount = 0 WHERE wordId = :id")
    fun markAsSolved(id: String)

    @Query("UPDATE words SET errorCount = errorCount + 1, lastAttemptTimestamp = :time WHERE wordId = :id")
    fun incrementError(id: String, time: Long)

    @Query("SELECT COUNT(*) FROM words WHERE level = :level AND isSolved = 1")
    fun getSolvedCount(level: GameLevel): Int

    @Query("SELECT COUNT(*) FROM words WHERE level = :level")
    fun getTotalCount(level: GameLevel): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWords(words: List<WordEntity>)
}

@Database(entities = [WordEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
