package com.drskur.vocabean.data.repository

import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 메모리 저장소 구현. 영구 저장을 붙이기 전까지 개발/프리뷰용으로 사용한다.
 * (DataStore 기반 구현체로 교체 예정)
 */
class InMemoryWordSetRepository(
    initialSets: List<WordSet> = SampleData.sets
) : WordSetRepository {

    private val _sets = MutableStateFlow(initialSets.sortedBy { it.createdAt })
    override val sets: StateFlow<List<WordSet>> = _sets.asStateFlow()

    override suspend fun getSet(id: String): WordSet? =
        _sets.value.find { it.id == id }

    override suspend fun upsertSet(set: WordSet) {
        _sets.update { current ->
            val index = current.indexOfFirst { it.id == set.id }
            if (index >= 0) {
                current.toMutableList().apply { set(index, set) }
            } else {
                current + set
            }
        }
    }

    override suspend fun deleteSet(id: String) {
        _sets.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun addWord(setId: String, word: Word) {
        updateWords(setId) { it + word }
    }

    override suspend fun updateWord(setId: String, word: Word) {
        updateWords(setId) { words -> words.map { if (it.id == word.id) word else it } }
    }

    override suspend fun deleteWord(setId: String, wordId: String) {
        updateWords(setId) { words -> words.filterNot { it.id == wordId } }
    }

    private fun updateWords(setId: String, transform: (List<Word>) -> List<Word>) {
        _sets.update { current ->
            current.map { set ->
                if (set.id == setId) set.copy(words = transform(set.words)) else set
            }
        }
    }
}
