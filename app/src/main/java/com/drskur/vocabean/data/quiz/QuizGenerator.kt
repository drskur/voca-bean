package com.drskur.vocabean.data.quiz

import com.drskur.vocabean.data.model.QuizMode
import com.drskur.vocabean.data.model.QuizQuestion
import com.drskur.vocabean.data.model.QuizType
import com.drskur.vocabean.data.model.WordSet
import kotlin.random.Random

/**
 * [WordSet]으로부터 랜덤 순서의 퀴즈 문제를 생성한다.
 * 문제는 저장하지 않고 퀴즈 시작 시마다 즉석에서 만든다.
 */
object QuizGenerator {

    /**
     * @param set    출제할 단어 세트
     * @param mode   퀴즈 모드
     * @param random 랜덤 시드 주입 가능 (테스트용)
     * @return 섞인 순서의 문제 목록 (세트의 모든 단어)
     */
    fun generate(
        set: WordSet,
        mode: QuizMode,
        random: Random = Random.Default
    ): List<QuizQuestion> {
        if (set.words.isEmpty()) return emptyList()

        return set.words.shuffled(random).map { word ->
            val type = when (mode) {
                QuizMode.MEANING -> QuizType.ENGLISH_TO_KOREAN
                QuizMode.ENGLISH -> QuizType.KOREAN_TO_ENGLISH
                QuizMode.MIXED -> if (random.nextBoolean()) {
                    QuizType.ENGLISH_TO_KOREAN
                } else {
                    QuizType.KOREAN_TO_ENGLISH
                }
            }
            QuizQuestion(word = word, type = type)
        }
    }
}
