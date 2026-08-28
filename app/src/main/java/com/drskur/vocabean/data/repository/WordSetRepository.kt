package com.drskur.vocabean.data.repository

import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import kotlinx.coroutines.flow.StateFlow

/**
 * 단어 세트 저장소. 영구 저장 방식(DataStore JSON, Room 등)은 이 인터페이스 뒤에 숨긴다.
 * UI는 [sets]를 구독해서 목록을 화면에 그린다.
 */
interface WordSetRepository {

    /** 전체 세트 목록 (생성 순). 변경 시 자동 갱신됨. */
    val sets: StateFlow<List<WordSet>>

    suspend fun getSet(id: String): WordSet?

    /** 새 세트 추가 또는 기존 세트 갱신 (id로 판별) */
    suspend fun upsertSet(set: WordSet)

    suspend fun deleteSet(id: String)

    suspend fun addWord(setId: String, word: Word)

    suspend fun updateWord(setId: String, word: Word)

    suspend fun deleteWord(setId: String, wordId: String)
}
