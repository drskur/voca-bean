package com.drskur.vocabean.data.quiz

import com.drskur.vocabean.data.model.QuizQuestion
import com.drskur.vocabean.data.model.QuizType
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import kotlin.random.Random

/**
 * [WordSet]으로부터 랜덤 퀴즈 문제를 생성한다.
 * 문제는 저장하지 않고 퀴즈 화면 진입 시마다 즉석에서 만든다.
 */
object QuizGenerator {

    /**
     * @param set            출제할 단어 세트
     * @param questionCount  문제 수 (기본: 세트의 모든 단어). 단어 수를 벗어나면 잘림.
     * @param random         랜덤 시드 주입 가능 (테스트용)
     */
    fun generate(
        set: WordSet,
        questionCount: Int = set.words.size,
        random: Random = Random.Default
    ): List<QuizQuestion> {
        if (set.words.isEmpty()) return emptyList()

        val count = questionCount.coerceIn(1, set.words.size)

        return set.words.shuffled(random).take(count).map { word ->
            val type = if (random.nextBoolean()) {
                QuizType.ENGLISH_TO_KOREAN
            } else {
                QuizType.KOREAN_TO_ENGLISH
            }
            val answer = answerOf(word, type)

            // 같은 세트의 다른 단어들로 오답 만들기 (정답과 중복되는 뜻은 제외)
            val distractors = set.words
                .filter { it.id != word.id }
                .map { answerOf(it, type) }
                .filter { it != answer }
                .distinct()
                .shuffled(random)
                .take(QuizQuestion.OPTION_COUNT - 1)

            val options = (distractors + answer).shuffled(random)
            QuizQuestion(
                word = word,
                type = type,
                options = options,
                answerIndex = options.indexOf(answer)
            )
        }
    }

    private fun answerOf(word: Word, type: QuizType): String = when (type) {
        QuizType.ENGLISH_TO_KOREAN -> word.korean
        QuizType.KOREAN_TO_ENGLISH -> word.english
    }
}
