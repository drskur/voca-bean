package com.drskur.vocabean

import com.drskur.vocabean.data.model.QuizType
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.quiz.QuizGenerator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizGeneratorTest {

    private val set = WordSet(
        id = "test",
        name = "테스트 세트",
        emoji = "🧪",
        words = listOf(
            Word("1", "apple", "사과"),
            Word("2", "banana", "바나나"),
            Word("3", "grape", "포도"),
            Word("4", "peach", "복숭아"),
            Word("5", "melon", "멜론"),
            Word("6", "mango", "망고")
        )
    )

    @Test
    fun `빈 세트는 빈 퀴즈를 반환한다`() {
        val empty = set.copy(words = emptyList())
        assertTrue(QuizGenerator.generate(empty).isEmpty())
    }

    @Test
    fun `문제 수는 단어 수를 넘을 수 없다`() {
        val questions = QuizGenerator.generate(set, questionCount = 100)
        assertEquals(set.wordCount, questions.size)
    }

    @Test
    fun `요청한 문제 수만큼 생성한다`() {
        assertEquals(3, QuizGenerator.generate(set, questionCount = 3).size)
    }

    @Test
    fun `정답이 항상 선택지에 포함되고 answerIndex가 가리킨다`() {
        repeat(50) { seed ->
            val questions = QuizGenerator.generate(set, random = Random(seed))
            questions.forEach { q ->
                val expected = when (q.type) {
                    QuizType.ENGLISH_TO_KOREAN -> q.word.korean
                    QuizType.KOREAN_TO_ENGLISH -> q.word.english
                }
                assertEquals(expected, q.options[q.answerIndex])
                assertEquals(expected, q.answer)
            }
        }
    }

    @Test
    fun `선택지에는 중복이 없다`() {
        repeat(50) { seed ->
            QuizGenerator.generate(set, random = Random(seed)).forEach { q ->
                assertEquals(q.options.size, q.options.distinct().size)
            }
        }
    }

    @Test
    fun `단어가 4개 이상이면 선택지는 4개다`() {
        val questions = QuizGenerator.generate(set, random = Random(42))
        questions.forEach { q ->
            assertEquals(4, q.options.size)
        }
    }

    @Test
    fun `단어가 적으면 선택지 개수가 줄어든다`() {
        val small = set.copy(
            words = listOf(
                Word("1", "apple", "사과"),
                Word("2", "banana", "바나나")
            )
        )
        val questions = QuizGenerator.generate(small, random = Random(1))
        assertEquals(2, questions.size)
        questions.forEach { q ->
            assertTrue(q.options.size in 1..2)
        }
    }

    @Test
    fun `같은 단어로 중복 출제되지 않는다`() {
        val questions = QuizGenerator.generate(set, random = Random(7))
        assertEquals(questions.size, questions.map { it.word.id }.distinct().size)
    }
}
