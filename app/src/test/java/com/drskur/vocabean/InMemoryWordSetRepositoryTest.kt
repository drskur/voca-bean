package com.drskur.vocabean

import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.model.QuizMode
import com.drskur.vocabean.data.quiz.QuizGenerator
import com.drskur.vocabean.data.repository.InMemoryWordSetRepository
import com.drskur.vocabean.data.repository.SampleData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryWordSetRepositoryTest {

    private fun repo() = InMemoryWordSetRepository()

    @Test
    fun `샘플 데이터로 시작한다`() = runBlocking {
        assertEquals(SampleData.sets.size, repo().sets.value.size)
    }

    @Test
    fun `세트를 추가하면 목록에 나타난다`() = runBlocking {
        val repository = InMemoryWordSetRepository(initialSets = emptyList())
        repository.upsertSet(
            WordSet(id = "s1", name = "탈것", emoji = "🚗", createdAt = 1L)
        )
        assertEquals(1, repository.sets.value.size)
        assertEquals("탈것", repository.sets.value.first().name)
    }

    @Test
    fun `같은 id로 upsert하면 갱신된다`() = runBlocking {
        val repository = InMemoryWordSetRepository(
            initialSets = listOf(WordSet(id = "s1", name = "탈것", emoji = "🚗", createdAt = 1L))
        )
        repository.upsertSet(
            WordSet(id = "s1", name = "빠른 탈것", emoji = "🚀", createdAt = 1L)
        )
        assertEquals(1, repository.sets.value.size)
        assertEquals("빠른 탈것", repository.sets.value.first().name)
    }

    @Test
    fun `세트를 삭제하면 목록에서 사라진다`() = runBlocking {
        val repository = repo()
        val id = repository.sets.value.first().id
        repository.deleteSet(id)
        assertNull(repository.getSet(id))
    }

    @Test
    fun `단어를 추가, 수정, 삭제할 수 있다`() = runBlocking {
        val repository = InMemoryWordSetRepository(
            initialSets = listOf(
                WordSet(
                    id = "s1", name = "탈것", emoji = "🚗", createdAt = 1L,
                    words = listOf(Word("w1", "car", "자동차", "🚗"))
                )
            )
        )

        repository.addWord("s1", Word("w2", "bus", "버스", "🚌"))
        assertEquals(2, repository.getSet("s1")?.wordCount)

        repository.updateWord("s1", Word("w2", "bus", "학교 버스", "🚌"))
        assertEquals("학교 버스", repository.getSet("s1")?.words?.find { it.id == "w2" }?.korean)

        repository.deleteWord("s1", "w2")
        assertEquals(1, repository.getSet("s1")?.wordCount)
    }

    @Test
    fun `목록은 생성 순으로 정렬된다`() = runBlocking {
        val repository = InMemoryWordSetRepository(
            initialSets = listOf(
                WordSet(id = "b", name = "나중", emoji = "⏰", createdAt = 2L),
                WordSet(id = "a", name = "먼저", emoji = "⏱️", createdAt = 1L)
            )
        )
        assertEquals(listOf("먼저", "나중"), repository.sets.value.map { it.name })
    }

    @Test
    fun `없는 세트 조회는 null을 반환한다`() = runBlocking {
        assertNull(repo().getSet("nope"))
    }

    @Test
    fun `샘플 세트는 모두 퀴즈 생성이 가능하다`() {
        SampleData.sets.forEach { set ->
            val questions = QuizGenerator.generate(set, QuizMode.MIXED)
            assertTrue("${set.name} 퀴즈 생성 패", questions.isNotEmpty())
            assertEquals(set.wordCount, questions.size)
        }
    }
}
