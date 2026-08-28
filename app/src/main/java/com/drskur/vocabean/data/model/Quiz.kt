package com.drskur.vocabean.data.model

/** 퀴즈 문제 유형 */
enum class QuizType {
    /** 영어 단어를 보고 한국어 뜻 고르기 */
    ENGLISH_TO_KOREAN,

    /** 한국어 뜻을 보고 영어 단어 고르기 */
    KOREAN_TO_ENGLISH
}

/**
 * 퀴즈 문제 한 개. [WordSet]으로부터 [com.drskur.vocabean.data.quiz.QuizGenerator]가 즉석 생성한다.
 *
 * @param word        출제된 단어
 * @param type        문제 유형
 * @param options     선택지 (선택지 텍스트만 담음, 기대 개수는 [OPTION_COUNT])
 * @param answerIndex 정답의 options 내 위치
 */
data class QuizQuestion(
    val word: Word,
    val type: QuizType,
    val options: List<String>,
    val answerIndex: Int
) {
    companion object {
        /** 기본 선택지 개수 (4지선다) */
        const val OPTION_COUNT = 4
    }

    val answer: String get() = options[answerIndex]
}
