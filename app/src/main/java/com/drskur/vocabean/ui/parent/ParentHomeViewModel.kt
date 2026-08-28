package com.drskur.vocabean.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.WordSetRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 보호자 설정: 세트 목록 관리 */
class ParentHomeViewModel(
    private val repository: WordSetRepository
) : ViewModel() {

    val sets: StateFlow<List<WordSet>> = repository.sets

    fun deleteSet(id: String) {
        viewModelScope.launch { repository.deleteSet(id) }
    }
}
