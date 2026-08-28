package com.drskur.vocabean.data.model

import java.util.UUID

/**
 * 단어 세트. 랜딩 → 세트 목록 → 퀴즈 흐름의 기본 단위.
 *
 * @param id         고유 식별자 (기본값: 자동 생성 UUID)
 * @param name       세트 이름 (예: "동물", "과일")
 * @param emoji      세트 대표 아이콘 (예: "🐶")
 * @param colorIndex 세트 목록 카드 색상 인덱스. UI가 파스텔 팔레트로 매핑.
 * @param createdAt  생성 시각 (epoch millis). 목록 정렬용.
 * @param words      세트에 속한 단어들
 */
data class WordSet(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val words: List<Word> = emptyList()
) {
    val wordCount: Int get() = words.size
}
