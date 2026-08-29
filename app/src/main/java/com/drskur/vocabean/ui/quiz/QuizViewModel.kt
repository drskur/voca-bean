package com.drskur.vocabean.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drskur.vocabean.data.model.QuizMode
import com.drskur.vocabean.data.model.QuizQuestion
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.quiz.QuizGenerator
import com.drskur.vocabean.data.repository.WordSetRepository
import com.drskur.vocabean.sound.SoundEffect
import com.drskur.vocabean.sound.SoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 퀴즈 화면 상태 */
data class QuizUiState(
    val loaded: Boolean = false,
    val setName: String = "",
    val setEmoji: String = "🌱",
    val wordCount: Int = 0,
    /** null이 아니면 진행 중 (null + finished null = 모드 선택) */
    val playing: PlayingState? = null,
    val finished: FinishedState? = null
) {
    data class PlayingState(
        val mode: QuizMode,
        val questions: List<QuizQuestion>,
        val index: Int = 0,
        val input: String = "",
        val feedback: Feedback? = null,
        val correctCount: Int = 0
    ) {
        val question: QuizQuestion get() = questions[index]
        val isLast: Boolean get() = index == questions.lastIndex

        /** 진행률 0~1 (푼 문제 포함) */
        val progress: Float
            get() = (index + if (feedback != null) 1 else 0).toFloat() / questions.size
    }

    data class FinishedState(val total: Int, val correct: Int, val mode: QuizMode)

    enum class Feedback { CORRECT, WRONG }
}

/** 퀴즈 진행 뷰모델: 모드 선택 → 풀이 → 결과 */
class QuizViewModel(
    private val repository: WordSetRepository,
    private val soundPlayer: SoundPlayer,
    private val setId: String
) : ViewModel() {

    private var loadedSet: WordSet? = null

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val set = repository.getSet(setId)
            loadedSet = set
            _state.update {
                it.copy(
                    loaded = true,
                    setName = set?.name.orEmpty(),
                    setEmoji = set?.emoji ?: "🌱",
                    wordCount = set?.wordCount ?: 0
                )
            }
        }
    }

    /** 모드 선택 후 퀴즈 시작 (단어를 섞어 문제를 만든다) */
    fun start(mode: QuizMode) {
        val set = loadedSet ?: return
        val questions = QuizGenerator.generate(set, mode)
        if (questions.isEmpty()) return
        _state.update {
            it.copy(
                playing = QuizUiState.PlayingState(mode = mode, questions = questions),
                finished = null
            )
        }
    }

    fun onInputChanged(text: String) {
        _state.update { st ->
            st.playing?.let { p ->
                if (p.feedback == null) st.copy(playing = p.copy(input = text)) else st
            } ?: st
        }
    }

    /** 채점. 빈 입력은 무시한다. */
    fun submit() {
        _state.update { st ->
            val p = st.playing ?: return@update st
            if (p.feedback != null || p.input.isBlank()) return@update st
            val correct = p.question.isCorrect(p.input)
            soundPlayer.play(if (correct) SoundEffect.CORRECT else SoundEffect.WRONG)
            st.copy(
                playing = p.copy(
                    feedback = if (correct) {
                        QuizUiState.Feedback.CORRECT
                    } else {
                        QuizUiState.Feedback.WRONG
                    },
                    correctCount = p.correctCount + if (correct) 1 else 0
                )
            )
        }
    }

    /** 다음 문제로. 마지막이면 결과 화면으로. */
    fun next() {
        _state.update { st ->
            val p = st.playing ?: return@update st
            if (p.feedback == null) return@update st
            if (p.isLast) {
                soundPlayer.play(SoundEffect.COMPLETE)
                st.copy(
                    playing = null,
                    finished = QuizUiState.FinishedState(
                        total = p.questions.size,
                        correct = p.correctCount,
                        mode = p.mode
                    )
                )
            } else {
                st.copy(playing = p.copy(index = p.index + 1, input = "", feedback = null))
            }
        }
    }

    /** 같은 모드로 다시 시작 (다시 섞는다) */
    fun retry() {
        val mode = _state.value.finished?.mode ?: return
        start(mode)
    }

    /** 모드 선택 화면으로 */
    fun backToModeSelect() {
        _state.update { it.copy(playing = null, finished = null) }
    }
}
