package com.drskur.vocabean.data.repository

import androidx.room.withTransaction
import com.drskur.vocabean.data.db.VocaBeanDatabase
import com.drskur.vocabean.data.db.WordSetDao
import com.drskur.vocabean.data.db.toDomain
import com.drskur.vocabean.data.db.toEntity
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Room 기반 [WordSetRepository] 구현체.
 *
 * @param dao    단어 세트 DAO
 * @param db     트랜잭션용 데이터베이스
 * @param scope  sets StateFlow 및 초기 시딩에 쓸 애플리케이션 스코프
 */
class RoomWordSetRepository(
    private val dao: WordSetDao,
    private val db: VocaBeanDatabase,
    private val scope: CoroutineScope
) : WordSetRepository {

    override val sets: StateFlow<List<WordSet>> = dao.observeSetsWithWords()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** 첫 실행 시 샘플 세트를 한 트랜잭션으로 삽입한다. 앱 시작 시 호출. */
    suspend fun seedIfEmpty() {
        db.withTransaction {
            if (dao.count() == 0) {
                SampleData.sets.forEach { set ->
                    dao.upsertSet(set.toEntity())
                    dao.upsertWords(set.words.map { it.toEntity(set.id) })
                }
            }
        }
    }

    override suspend fun getSet(id: String): WordSet? =
        dao.getSetWithWords(id)?.toDomain()

    override suspend fun upsertSet(set: WordSet) {
        db.withTransaction {
            dao.upsertSet(set.toEntity())

            // 단어 목록 전체 동기화: 목록에서 빠진 단어는 삭제
            val existingIds = dao.getSetWithWords(set.id)?.words?.map { it.id }?.toSet()
                ?: emptySet()
            val newIds = set.words.map { it.id }.toSet()
            (existingIds - newIds).forEach { dao.deleteWord(it) }

            dao.upsertWords(set.words.map { it.toEntity(set.id) })
        }
    }

    override suspend fun deleteSet(id: String) {
        dao.deleteSet(id) // 단어는 CASCADE로 함께 삭제
    }

    override suspend fun addWord(setId: String, word: Word) {
        dao.upsertWords(listOf(word.toEntity(setId)))
    }

    override suspend fun updateWord(setId: String, word: Word) {
        dao.upsertWords(listOf(word.toEntity(setId)))
    }

    override suspend fun deleteWord(setId: String, wordId: String) {
        dao.deleteWord(wordId)
    }
}
