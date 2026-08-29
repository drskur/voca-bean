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
        assertEquals(meaning.word.primaryKorean, meaning.answer)

        val english = QuizGenerator.generate(set, QuizMode.ENGLISH, Random(7)).first()
        assertEquals(english.word.displayKorean, english.prompt)
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
            .first { "사과" in it.word.koreans }
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

    @Test
    fun `채점 - 한국어 뜻이 여러 개면 어느 하나만 맞아도 정답이다`() {
        val multi = WordSet(
            id = "multi",
            name = "여러 뜻",
            emoji = "🌈",
            words = listOf(
                Word(id = "a", english = "apple", koreans = listOf("사과", "능금"), emoji = "🍎")
            )
        )
        val meaning = QuizGenerator.generate(multi, QuizMode.MEANING, Random(1)).first()
        assertEquals(QuizType.ENGLISH_TO_KOREAN, meaning.type)
        assertEquals("apple", meaning.prompt)
        assertEquals("사과, 능금", meaning.answerDisplay)
        assertTrue(meaning.isCorrect("사과"))
        assertTrue(meaning.isCorrect("능금"))
        assertTrue(meaning.isCorrect("  능금  "))
        assertFalse(meaning.isCorrect("사과, 능금"))
        assertFalse(meaning.isCorrect("배"))

        // 영어 쓰기 모드에서는 프롬프트가 모든 뜻을 보여준다
        val english = QuizGenerator.generate(multi, QuizMode.ENGLISH, Random(1)).first()
        assertEquals(QuizType.KOREAN_TO_ENGLISH, english.type)
        assertEquals("사과, 능금", english.prompt)
        assertEquals("apple", english.answerDisplay)
        assertTrue(english.isCorrect("apple"))
    }
}
