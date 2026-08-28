package com.drskur.vocabean

import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.InMemoryWordSetRepository
import com.drskur.vocabean.ui.parent.SetEditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetEditorViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `새 세트를 만들어 저장한다`() = runTest(dispatcher) {
        val repository = InMemoryWordSetRepository(initialSets = emptyList())
        val viewModel = SetEditorViewModel(repository, setId = null)

        viewModel.setName("동물 친구들")
        viewModel.setEmoji("🐶")
        viewModel.setColor(3)
        assertTrue(viewModel.addWord("dog", "강아지", "🐶"))
        assertTrue(viewModel.addWord("cat", "고양이", null))

        var saved = false
        viewModel.save { saved = true }

        assertTrue(saved)
        val stored = repository.sets.value.single()
        assertEquals("동물 친구들", stored.name)
        assertEquals("🐶", stored.emoji)
        assertEquals(3, stored.colorIndex)
        assertEquals(listOf("dog", "cat"), stored.words.map { it.english })
        assertEquals(listOf("🐶", null), stored.words.map { it.emoji })
    }

    @Test
    fun `기존 세트 편집 시 id와 createdAt을 유지한다`() = runTest(dispatcher) {
        val original = WordSet(
            id = "s1", name = "동물", emoji = "🐶", colorIndex = 0, createdAt = 123L,
            words = listOf(Word("w1", "dog", "강아지"))
        )
        val repository = InMemoryWordSetRepository(initialSets = listOf(original))
        val viewModel = SetEditorViewModel(repository, setId = "s1")

        assertTrue(viewModel.state.value.loaded)
        viewModel.setName("우리 동물")
        viewModel.removeWord("w1")
        viewModel.addWord("cat", "고양이", null)
        viewModel.save { }

        val stored = repository.sets.value.single()
        assertEquals("s1", stored.id)
        assertEquals(123L, stored.createdAt)
        assertEquals("우리 동물", stored.name)
        assertEquals(listOf("cat"), stored.words.map { it.english })
    }

    @Test
    fun `이름이 비어 있으면 저장되지 않는다`() = runTest(dispatcher) {
        val repository = InMemoryWordSetRepository(initialSets = emptyList())
        val viewModel = SetEditorViewModel(repository, setId = null)
        viewModel.addWord("dog", "강아지", null)

        var saved = false
        viewModel.save { saved = true }

        assertFalse(saved)
        assertTrue(repository.sets.value.isEmpty())
    }

    @Test
    fun `빈 단어는 추가되지 않는다`() = runTest(dispatcher) {
        val repository = InMemoryWordSetRepository(initialSets = emptyList())
        val viewModel = SetEditorViewModel(repository, setId = null)

        assertFalse(viewModel.addWord("", "강아지", null))
        assertFalse(viewModel.addWord("dog", "   ", null))
        assertTrue(viewModel.state.value.words.isEmpty())
    }

    @Test
    fun `이모지는 공백이면 null로 저장된다`() = runTest(dispatcher) {
        val repository = InMemoryWordSetRepository(initialSets = emptyList())
        val viewModel = SetEditorViewModel(repository, setId = null)

        assertTrue(viewModel.addWord("dog", "강아지", "  "))
        assertEquals(null, viewModel.state.value.words.single().emoji)
    }

    @Test
    fun `존재하지 않는 세트 id는 새 세트로 취급한다`() = runTest(dispatcher) {
        val repository = InMemoryWordSetRepository(initialSets = emptyList())
        val viewModel = SetEditorViewModel(repository, setId = "ghost")

        assertTrue(viewModel.state.value.loaded)
        assertTrue(viewModel.state.value.isNew)
    }
}
