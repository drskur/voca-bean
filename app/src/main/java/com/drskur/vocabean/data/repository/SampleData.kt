package com.drskur.vocabean.data.repository

import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet

/**
 * 기본 제공 샘플 세트. 앱 첫 실행 시 보여줄 초급 단어들.
 */
object SampleData {

    val sets: List<WordSet> = listOf(
        WordSet(
            id = "sample-animals",
            name = "동물 친구들",
            emoji = "🐶",
            colorIndex = 0,
            createdAt = 1_000L,
            words = listOf(
                Word("sample-animal-1", "dog", "강아지", "🐶"),
                Word("sample-animal-2", "cat", "고양이", "🐱"),
                Word("sample-animal-3", "rabbit", "토끼", "🐰"),
                Word("sample-animal-4", "bird", "새", "🐦"),
                Word("sample-animal-5", "fish", "물고기", "🐟"),
                Word("sample-animal-6", "bear", "곰", "🐻")
            )
        ),
        WordSet(
            id = "sample-fruits",
            name = "맛있는 과일",
            emoji = "🍎",
            colorIndex = 1,
            createdAt = 2_000L,
            words = listOf(
                Word("sample-fruit-1", "apple", "사과", "🍎"),
                Word("sample-fruit-2", "banana", "바나나", "🍌"),
                Word("sample-fruit-3", "grape", "포도", "🍇"),
                Word("sample-fruit-4", "watermelon", "수박", "🍉"),
                Word("sample-fruit-5", "strawberry", "딸기", "🍓"),
                Word("sample-fruit-6", "peach", "복숭아", "🍑")
            )
        ),
        WordSet(
            id = "sample-colors",
            name = "알록달록 색깔",
            emoji = "🌈",
            colorIndex = 2,
            createdAt = 3_000L,
            words = listOf(
                Word("sample-color-1", "red", "빨간색", "❤️"),
                Word("sample-color-2", "blue", "파란색", "💙"),
                Word("sample-color-3", "yellow", "노란색", "💛"),
                Word("sample-color-4", "green", "초록색", "💚"),
                Word("sample-color-5", "pink", "분홍색", "🩷"),
                Word("sample-color-6", "purple", "보라색", "💜")
            )
        )
    )
}
