package com.drskur.vocabean.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drskur.vocabean.ui.theme.InkBrown

/** 리플 없는 클릭 modifier (앱 전체 터치 피드백 정책) */
@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}

/** 흰 원형 뒤로가기 버튼 */
@Composable
fun CircleBackButton(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "←",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown
        )
    }
}
