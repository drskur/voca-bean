package com.drskur.vocabean.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.WordSetRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 세트 편집 화면 상태. 저장 전까지 로컬에서만 수정한다. */
data class SetEditorUiState(
    val isNew: Boolean,
    val loaded: Boolean,
    val name: String = "",
    val emoji: String = "⭐",
    val colorIndex: Int = 0,
    val words: List<Word> = emptyList()
)

/**
 * 세트 생성/편집 뷰모델.
 *
 * 편집 대상은 [setId]로 구분하며(null이면 새 세트),
 * 저장 시 세트 전체를 upsert해 단어 목록이 통째로 동기화된다.
 */
class SetEditorViewModel(
    private val repository: WordSetRepository,
    private val setId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(
        SetEditorUiState(isNew = setId == null, loaded = setId == null)
    )
    val state: StateFlow<SetEditorUiState> = _state.asStateFlow()

    init {
        if (setId != null) {
            viewModelScope.launch {
                val set = repository.getSet(setId)
                _state.update {
                    if (set != null) {
                        it.copy(
                            name = set.name,
                            emoji = set.emoji,
                            colorIndex = set.colorIndex,
                            words = set.words,
                            loaded = true
                        )
                    } else {
                        // 존재하지 않는 id면 새 세트로 취급
                        it.copy(isNew = true, loaded = true)
                    }
                }
            }
        }
    }

    fun setName(name: String) = _state.update { it.copy(name = name) }

    fun setEmoji(emoji: String) = _state.update { it.copy(emoji = emoji) }

    fun setColor(colorIndex: Int) = _state.update { it.copy(colorIndex = colorIndex) }

    /**
     * 단어 추가. 뜻은 쉼표로 여러 개 입력할 수 있다.
     * 영어와 뜻이 모두 하나 이상 있어야 true.
     */
    fun addWord(english: String, korean: String, emoji: String?): Boolean {
        val e = english.trim()
        val meanings = korean.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (e.isEmpty() || meanings.isEmpty()) return false
        _state.update {
            it.copy(
                words = it.words + Word(
                    english = e,
                    koreans = meanings,
                    emoji = emoji?.trim()?.takeIf(String::isNotEmpty)
                )
            )
        }
        return true
    }

    fun removeWord(wordId: String) =
        _state.update { it.copy(words = it.words.filterNot { w -> w.id == wordId }) }

    /** 저장. 이름이 비어 있으면 저장하지 않고, 성공 시 [onSaved]를 호출한다. */
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.loaded || current.name.isBlank()) return

        viewModelScope.launch {
            val existing = setId?.let { repository.getSet(it) }
            repository.upsertSet(
                WordSet(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    name = current.name.trim(),
                    emoji = current.emoji,
                    colorIndex = current.colorIndex,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    words = current.words
                )
            )
            onSaved()
        }
    }
}
