package com.drskur.vocabean.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 세트 카드·아이콘 배경에 쓰는 파스텔 팔레트.
 * [com.drskur.vocabean.data.model.WordSet.colorIndex]를 나머지 연산으로 매핑한다.
 */
val SetPalette = listOf(
    Color(0xFFFFD6E0), // 분홍
    Color(0xFFFFF1B8), // 노랑
    Color(0xFFC9F2C3), // 연두
    Color(0xFFC3E5FF), // 하늘
    Color(0xFFE3D2FF), // 보라
    Color(0xFFFFD9C2)  // 주황
)

fun setPaletteColor(index: Int): Color = SetPalette[index % SetPalette.size]
