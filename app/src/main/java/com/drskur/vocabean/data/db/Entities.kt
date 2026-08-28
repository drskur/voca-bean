package com.drskur.vocabean.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** 단어 세트 테이블 */
@Entity(tableName = "word_sets")
data class WordSetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val colorIndex: Int,
    val createdAt: Long
)

/** 단어 테이블. 세트 삭제 시 자식 단어도 함께 삭제(CASCADE). */
@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = WordSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setId")]
)
data class WordEntity(
    @PrimaryKey val id: String,
    val setId: String,
    val english: String,
    val korean: String,
    val emoji: String?
)

/** 세트 + 소속 단어를 한 번에 조회하기 위한 릴레이션 */
data class WordSetWithWords(
    @Embedded val set: WordSetEntity,
    @Relation(parentColumn = "id", entityColumn = "setId")
    val words: List<WordEntity>
)
