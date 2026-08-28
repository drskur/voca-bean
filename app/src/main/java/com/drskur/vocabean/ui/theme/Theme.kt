package com.drskur.vocabean.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 어린이 타겟 앱이므로 항상 밝은 테마만 사용한다.
 * (다크모드, 다이나믹 컬러 미지원 - 브랜드 파스텔 컬러 유지)
 */
private val KidsColorScheme = lightColorScheme(
    primary = BeanGreen,
    onPrimary = Color.White,
    primaryContainer = BeanLight,
    onPrimaryContainer = LeafDark,
    secondary = SunnyYellow,
    onSecondary = InkBrown,
    secondaryContainer = Color(0xFFFFE9B8),
    onSecondaryContainer = InkBrown,
    tertiary = BubblePink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDE5),
    onTertiaryContainer = Color(0xFF5B2434),
    background = CreamBottom,
    onBackground = InkBrown,
    surface = Color.White,
    onSurface = InkBrown,
    surfaceVariant = Color(0xFFF3ECE4),
    onSurfaceVariant = InkBrown,
    error = Color(0xFFE5484D),
    onError = Color.White
)

@Composable
fun VocaBeanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KidsColorScheme,
        typography = Typography,
        content = content
    )
}
