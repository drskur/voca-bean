package com.drskur.vocabean.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WordSetDao {

    @Transaction
    @Query("SELECT * FROM word_sets ORDER BY createdAt ASC")
    fun observeSetsWithWords(): Flow<List<WordSetWithWords>>

    @Transaction
    @Query("SELECT * FROM word_sets WHERE id = :id")
    suspend fun getSetWithWords(id: String): WordSetWithWords?

    @Query("SELECT COUNT(*) FROM word_sets")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertSet(set: WordSetEntity)

    @Upsert
    suspend fun upsertWords(words: List<WordEntity>)

    @Query("DELETE FROM word_sets WHERE id = :id")
    suspend fun deleteSet(id: String)

    @Query("DELETE FROM words WHERE id = :wordId")
    suspend fun deleteWord(wordId: String)

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWord(wordId: String): WordEntity?
}
