package com.drskur.vocabean

import com.drskur.vocabean.data.model.QuizMode
import com.drskur.vocabean.data.model.QuizType
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.quiz.QuizGenerator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertTrue(QuizGenerator.generate(set.copy(words = emptyList()), QuizMode.MIXED).isEmpty())
    }

    @Test
    fun `모든 단어가 한 번씩 나온다`() {
        repeat(20) { seed ->
            val questions = QuizGenerator.generate(set, QuizMode.MIXED, Random(seed))
            assertEquals(set.words.size, questions.size)
            assertEquals(
                set.words.map { it.id }.toSet(),
                questions.map { it.word.id }.toSet()
            )
        }
    }

    @Test
    fun `뜻 맞히기 모드는 전부 영어에서 한국말로 낸다`() {
        val questions = QuizGenerator.generate(set, QuizMode.MEANING, Random(1))
        assertTrue(questions.all { it.type == QuizType.ENGLISH_TO_KOREAN })
    }

    @Test
    fun `영어 쓰기 모드는 전부 한국말에서 영어로 낸다`() {
        val questions = QuizGenerator.generate(set, QuizMode.ENGLISH, Random(1))
        assertTrue(questions.all { it.type == QuizType.KOREAN_TO_ENGLISH })
    }

    @Test
    fun `섞어 내기는 두 유형만 나오고 시드마다 조합이 달라진다`() {
        var sawBoth = false
        repeat(30) { seed ->
            val questions = QuizGenerator.generate(set, QuizMode.MIXED, Random(seed))
            val types = questions.map { it.type }.toSet()
            assertTrue(types.all {
                it == QuizType.ENGLISH_TO_KOREAN || it == QuizType.KOREAN_TO_ENGLISH
            })
            if (types.size == 2) sawBoth = true
        }
        assertTrue("30개 시드 안에 두 유형이 섞인 경우가 있어야 한다", sawBoth)
    }

    @Test
    fun `prompt와 answer가 유형에 맞게 나온다`() {
        val meaning = QuizGenerator.generate(set, QuizMode.MEANING, Random(7)).first()
        assertEquals(meaning.word.english, meaning.prompt)
        assertEquals(meaning.word.korean, meaning.answer)

        val english = QuizGenerator.generate(set, QuizMode.ENGLISH, Random(7)).first()
        assertEquals(english.word.korean, english.prompt)
        assertEquals(english.word.english, english.answer)
    }

    @Test
    fun `채점 - 영어는 대소문자와 공백을 무시한다`() {
        val question = QuizGenerator.generate(set, QuizMode.ENGLISH, Random(1))
            .first { it.word.english == "apple" }
        assertTrue(question.isCorrect("apple"))
        assertTrue(question.isCorrect("Apple"))
        assertTrue(question.isCorrect("  APPLE  "))
        assertFalse(question.isCorrect("apples"))
        assertFalse(question.isCorrect("사과"))
    }

    @Test
    fun `채점 - 한국말은 글자 그대로 비교한다`() {
        val question = QuizGenerator.generate(set, QuizMode.MEANING, Random(1))
            .first { it.word.korean == "사과" }
        assertTrue(question.isCorrect("사과"))
        assertTrue(question.isCorrect("  사과  "))
        assertFalse(question.isCorrect("사과요"))
        assertFalse(question.isCorrect("apple"))
    }

    @Test
    fun `빈 입력은 오답이다`() {
        val question = QuizGenerator.generate(set, QuizMode.MEANING, Random(1)).first()
        assertFalse(question.isCorrect(""))
        assertFalse(question.isCorrect("   "))
    }
}
