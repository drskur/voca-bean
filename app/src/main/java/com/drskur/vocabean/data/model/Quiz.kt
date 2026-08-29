package com.drskur.vocabean.data.model

/** 퀴즈 모드. 아이에게 보여주는 이름은 UI에서 매핑한다. */
enum class QuizMode {
    /** 영어 단어를 보고 뜻(한국말) 쓰기 */
    MEANING,

    /** 뜻(한국말)을 보고 영어 단어 쓰기 */
    ENGLISH,

    /** 두 유형을 섞어 내기 */
    MIXED
}

/** 문제 유형 */
enum class QuizType {
    /** 영어 → 한국말 */
    ENGLISH_TO_KOREAN,

    /** 한국말 → 영어 */
    KOREAN_TO_ENGLISH
}

/**
 * 퀴즈 문제 한 개 (주관식).
 * [com.drskur.vocabean.data.quiz.QuizGenerator]가 세트를 섞어서 즉석 생성한다.
 */
data class QuizQuestion(
    val word: Word,
    val type: QuizType
) {
    /** 문제에 보여줄 것 */
    val prompt: String
        get() = when (type) {
            QuizType.ENGLISH_TO_KOREAN -> word.english
            QuizType.KOREAN_TO_ENGLISH -> word.korean
        }

    /** 정답 */
    val answer: String
        get() = when (type) {
            QuizType.ENGLISH_TO_KOREAN -> word.korean
            QuizType.KOREAN_TO_ENGLISH -> word.english
        }

    /**
     * 주관식 채점.
     * - 공통: 양쪽 공백 제거, 빈 입력은 오답
     * - 영어 정답: 대소문자 구분 없음
     * - 한국말 정답: 글자 그대로 비교
     */
    fun isCorrect(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return false
        return when (type) {
            QuizType.KOREAN_TO_ENGLISH -> trimmed.equals(answer.trim(), ignoreCase = true)
            QuizType.ENGLISH_TO_KOREAN -> trimmed == answer.trim()
        }
    }
}
