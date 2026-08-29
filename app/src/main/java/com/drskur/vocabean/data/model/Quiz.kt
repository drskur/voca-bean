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
            // 한국말을 보고 영어를 쓸 때는 뜻이 여러 개면 모두 보여준다
            QuizType.KOREAN_TO_ENGLISH -> word.displayKorean
        }

    /** 채점 기준이 되는 수용 정답 목록 */
    private val acceptedAnswers: List<String>
        get() = when (type) {
            QuizType.ENGLISH_TO_KOREAN -> word.koreans
            QuizType.KOREAN_TO_ENGLISH -> listOf(word.english)
        }

    /** 대표 정답 (표시·테스트용 첫 값) */
    val answer: String get() = acceptedAnswers.firstOrNull() ?: ""

    /** 오답 피드백에 보여줄 정답. 한국말 뜻이 여러 개면 모두 보여준다. */
    val answerDisplay: String
        get() = when (type) {
            QuizType.ENGLISH_TO_KOREAN -> word.displayKorean
            QuizType.KOREAN_TO_ENGLISH -> word.english
        }

    /**
     * 주관식 채점.
     * - 공통: 양쪽 공백 제거, 빈 입력은 오답
     * - 영어 정답: 대소문자 구분 없음
     * - 한국말 정답(뜻 맞히기): [Word.koreans] 중 하나만 맞아도 정답, 글자 그대로 비교
     */
    fun isCorrect(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return false
        return when (type) {
            QuizType.KOREAN_TO_ENGLISH ->
                acceptedAnswers.any { trimmed.equals(it.trim(), ignoreCase = true) }
            QuizType.ENGLISH_TO_KOREAN ->
                acceptedAnswers.any { trimmed == it.trim() }
        }
    }
}
