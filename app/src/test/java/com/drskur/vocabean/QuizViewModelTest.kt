package com.drskur.vocabean

import com.drskur.vocabean.data.model.QuizMode
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.InMemoryWordSetRepository
import com.drskur.vocabean.sound.SoundEffect
import com.drskur.vocabean.sound.SoundPlayer
import com.drskur.vocabean.ui.quiz.QuizUiState
import com.drskur.vocabean.ui.quiz.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val repository = InMemoryWordSetRepository(
        initialSets = listOf(
            WordSet(
                id = "s1", name = "동물", emoji = "🐶", createdAt = 1L,
                words = listOf(
                    Word("1", "dog", "강아지", "🐶"),
                    Word("2", "cat", "고양이", "🐱"),
                    Word("3", "bird", "새", "🐦")
                )
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = QuizViewModel(repository, soundPlayer, "s1")

    private val soundPlayer = RecordingSoundPlayer()

    /** 테스트용: 재생된 효과음을 기록한다 */
    private class RecordingSoundPlayer : SoundPlayer {
        val played = mutableListOf<SoundEffect>()
        override fun play(effect: SoundEffect) {
            played += effect
        }
    }

    @Test
    fun `시작하면 세트 정보가 로드된다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val state = viewModel.state.value
        assertTrue(state.loaded)
        assertEquals("동물", state.setName)
        assertEquals(3, state.wordCount)
        assertNull(state.playing)
        assertNull(state.finished)
    }

    @Test
    fun `모드를 고르면 모든 단어가 섞여 출제된다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        val playing = viewModel.state.value.playing
        assertNotNull(playing)
        assertEquals(3, playing!!.questions.size)
    }

    @Test
    fun `정답을 쓰면 맞았다고 표시되고 점수가 오른다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        val question = viewModel.state.value.playing!!.question
        viewModel.onInputChanged(question.answer)
        viewModel.submit()

        val playing = viewModel.state.value.playing!!
        assertEquals(QuizUiState.Feedback.CORRECT, playing.feedback)
        assertEquals(1, playing.correctCount)
        assertEquals(listOf(SoundEffect.CORRECT), soundPlayer.played)
    }

    @Test
    fun `오답을 쓰면 정답이 함께 표시된다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        viewModel.onInputChanged("틀린답")
        viewModel.submit()

        val playing = viewModel.state.value.playing!!
        assertEquals(QuizUiState.Feedback.WRONG, playing.feedback)
        assertEquals(0, playing.correctCount)
        assertEquals(listOf(SoundEffect.WRONG), soundPlayer.played)
    }

    @Test
    fun `빈 입력은 채점되지 않는다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        viewModel.onInputChanged("   ")
        viewModel.submit()

        assertNull(viewModel.state.value.playing!!.feedback)
    }

    @Test
    fun `다음 문제로 넘어가면 입력과 피드백이 초기화된다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        val question = viewModel.state.value.playing!!.question
        viewModel.onInputChanged(question.answer)
        viewModel.submit()
        viewModel.next()

        val playing = viewModel.state.value.playing!!
        assertEquals(1, playing.index)
        assertEquals("", playing.input)
        assertNull(playing.feedback)
    }

    @Test
    fun `마지막 문제 다음에는 결과 화면으로 간다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        repeat(3) {
            val question = viewModel.state.value.playing!!.question
            viewModel.onInputChanged(question.answer)
            viewModel.submit()
            viewModel.next()
        }

        val state = viewModel.state.value
        assertNull(state.playing)
        assertNotNull(state.finished)
        assertEquals(3, state.finished!!.total)
        assertEquals(3, state.finished!!.correct)
        assertEquals(SoundEffect.COMPLETE, soundPlayer.played.last())
    }

    @Test
    fun `한 번 더 하면 같은 모드로 다시 섞어 출제한다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        repeat(3) {
            val question = viewModel.state.value.playing!!.question
            viewModel.onInputChanged(question.answer)
            viewModel.submit()
            viewModel.next()
        }
        viewModel.retry()

        val playing = viewModel.state.value.playing
        assertNotNull(playing)
        assertEquals(QuizMode.MEANING, playing!!.mode)
        assertEquals(0, playing.index)
    }

    @Test
    fun `모드 선택으로 돌아가면 진행 상태가 초기화된다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)
        viewModel.backToModeSelect()

        assertNull(viewModel.state.value.playing)
        assertNull(viewModel.state.value.finished)
    }

    @Test
    fun `답변 후에는 입력이 바뀌지 않는다`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.start(QuizMode.MEANING)

        val question = viewModel.state.value.playing!!.question
        viewModel.onInputChanged(question.answer)
        viewModel.submit()
        viewModel.onInputChanged("바꾸기 시도")

        assertEquals(question.answer, viewModel.state.value.playing!!.input)
    }
}
