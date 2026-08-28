package com.drskur.vocabean.data.model

import java.util.UUID

/**
 * 영어 단어 하나.
 *
 * @param id      고유 식별자 (기본값: 자동 생성 UUID)
 * @param english 영어 단어 (예: "apple")
 * @param korean  한국어 뜻 (예: "사과")
 * @param emoji   시각 힌트용 이모지 (예: "🍎") — 어린이 앱 특성상 선택 제공
 */
data class Word(
    val id: String = UUID.randomUUID().toString(),
    val english: String,
    val korean: String,
    val emoji: String? = null
)
