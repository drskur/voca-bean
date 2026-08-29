package com.drskur.vocabean.data.model

import java.util.UUID

/**
 * 영어 단어 하나.
 *
 * @param id      고유 식별자 (기본값: 자동 생성 UUID)
 * @param english 영어 단어 (예: "apple")
 * @param koreans 한국어 뜻 목록 (예: ["사과", "능금"]). 한 단어에 여러 뜻을 가질 수 있다.
 * @param emoji   시각 힌트용 이모지 (예: "🍎") — 어린이 앱 특성상 선택 제공
 */
data class Word(
    val id: String = UUID.randomUUID().toString(),
    val english: String,
    val koreans: List<String>,
    val emoji: String? = null
) {
    /**
     * 단일 뜻으로 만드는 편의 생성자. 뜻이 하나뿐일 때 쓴다.
     * (뜻이 여러 개면 [koreans]를 직접 지정하는 주 생성자를 쓴다.)
     */
    constructor(id: String, english: String, korean: String, emoji: String? = null) : this(
        id = id,
        english = english,
        koreans = listOf(korean),
        emoji = emoji
    )

    /** 대표 뜻 (첫 번째). 뜻이 없으면 빈 문자열. */
    val primaryKorean: String get() = koreans.firstOrNull() ?: ""

    /** 화면 표시용 — 뜻을 ", "로 이은 문자열. */
    val displayKorean: String get() = koreans.joinToString(", ")
}
