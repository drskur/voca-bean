package com.drskur.vocabean.data.db

import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet

/** 도메인 모델 ↔ Room 엔티티 변환 */

internal fun WordSet.toEntity(): WordSetEntity = WordSetEntity(
    id = id,
    name = name,
    emoji = emoji,
    colorIndex = colorIndex,
    createdAt = createdAt
)

internal fun Word.toEntity(setId: String): WordEntity = WordEntity(
    id = id,
    setId = setId,
    english = english,
    // 뜻이 여러 개면 쉼표로 이어 한 칼럼에 저장한다.
    korean = koreans.joinToString(", "),
    emoji = emoji
)

internal fun WordSetWithWords.toDomain(): WordSet = WordSet(
    id = set.id,
    name = set.name,
    emoji = set.emoji,
    colorIndex = set.colorIndex,
    createdAt = set.createdAt,
    words = words.map { it.toDomain() }
)

internal fun WordEntity.toDomain(): Word = Word(
    id = id,
    english = english,
    koreans = korean.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() },
    emoji = emoji
)
