package com.drskur.vocabean.ui.landing

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drskur.vocabean.ui.theme.BeanGreen
import com.drskur.vocabean.ui.theme.BeanLight
import com.drskur.vocabean.ui.theme.BubblePink
import com.drskur.vocabean.ui.theme.CreamBottom
import com.drskur.vocabean.ui.theme.GrapePurple
import com.drskur.vocabean.ui.theme.InkBrown
import com.drskur.vocabean.ui.theme.LeafDark
import com.drskur.vocabean.ui.theme.OceanBlue
import com.drskur.vocabean.ui.theme.SkyMiddle
import com.drskur.vocabean.ui.theme.SkyTop
import com.drskur.vocabean.ui.theme.SunnyYellow
import com.drskur.vocabean.ui.theme.Tangerine
import com.drskur.vocabean.ui.theme.VocaBeanTheme

@Composable
fun LandingScreen(
    onStartClick: () -> Unit = {},
    onParentClick: () -> Unit = {}
) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(SkyTop, SkyMiddle, CreamBottom)
                    )
                )
        ) {
            // ── 뒤쪽 언덕 ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        Color(0xFFB8EC9E),
                        RoundedCornerShape(topStart = 110.dp, topEnd = 110.dp)
                    )
            )
            // ── 앞쪽 언덕 ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Color(0xFF8FDD6E),
                        RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
                    )
            )

            // ── 하늘 장식: 구름 ──
            SwayingEmoji(text = "☁️", fontSize = 46.sp, durationMillis = 8500,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 14.dp, y = 46.dp))
            SwayingEmoji(text = "☁️", fontSize = 34.sp, durationMillis = 11000,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-26).dp, y = 104.dp))
            SwayingEmoji(text = "☁️", fontSize = 28.sp, durationMillis = 9500,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 10.dp, y = (-140).dp))

            // ── 하늘 장식: 반짝이는 별 ──
            TwinklingEmoji(text = "✨", fontSize = 22.sp, durationMillis = 1200,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-52).dp, y = 190.dp))
            TwinklingEmoji(text = "⭐", fontSize = 18.sp, durationMillis = 1600,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 30.dp, y = (-60).dp))
            TwinklingEmoji(text = "🌟", fontSize = 20.sp, durationMillis = 1900,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-18).dp, y = (-230).dp))

            // ── 풍선 ──
            BobbingEmoji(text = "🎈", fontSize = 34.sp, durationMillis = 3000,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 22.dp, y = 210.dp))

            // ── 언덕 위 꽃 ──
            SwayingFlower(text = "🌼", modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-96).dp))
            SwayingFlower(text = "🌷", modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-38).dp, y = (-120).dp))
            SwayingFlower(text = "🌻", modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 84.dp, y = (-44).dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BeanCharacter()
                        Spacer(modifier = Modifier.height(10.dp))
                        RainbowTitle(text = "VocaBean")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "콩이와 함께하는 신나는 영어 놀이터!",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBrown
                        )
                    }
                }

                // ── 시작 버튼 영역 (언덕 위) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PulsingStartButton(onClick = onStartClick)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "보호자 설정",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkBrown.copy(alpha = 0.75f),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onParentClick
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ── 콩 캐릭터 ────────────────────────────────────────────

@Composable
fun BeanCharacter(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bean")
    val bounceP by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1150
                0f at 0 using FastOutSlowInEasing
                1f at 520 using FastOutSlowInEasing
                0f at 1150 using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bounceP"
    )

    // 콩이를 탭하면 신나서 한 바퀴 돈다
    var spins by remember { mutableIntStateOf(0) }
    val spin by animateFloatAsState(
        targetValue = spins * 360f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "spin"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .size(width = 168.dp, height = 176.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { spins++ }
                .graphicsLayer {
                    translationY = -30.dp.toPx() * bounceP
                    scaleX = 1f - 0.06f * bounceP
                    scaleY = 1f + 0.09f * bounceP
                    rotationY = spin
                }
        ) {
            drawBean()
        }
        // 그림자 (점프 높이에 따라 작아진다)
        Canvas(modifier = Modifier.size(width = 112.dp, height = 22.dp)) {
            val w = size.width * (0.80f - 0.28f * bounceP)
            val h = size.height * (0.60f - 0.20f * bounceP)
            drawOval(
                color = Color(0xFF2E4A2E).copy(alpha = 0.28f - 0.16f * bounceP),
                topLeft = Offset((size.width - w) / 2f, (size.height - h) / 2f),
                size = Size(w, h)
            )
        }
    }
}

private fun DrawScope.drawBean() {
    val w = size.width
    val h = size.height

    val bodyLeft = w * 0.06f
    val bodyTop = h * 0.16f
    val bodyW = w * 0.88f
    val bodyH = h * 0.74f

    // 새싹 줄기
    drawLine(
        color = LeafDark,
        start = Offset(w * 0.5f, bodyTop + h * 0.015f),
        end = Offset(w * 0.5f, h * 0.075f),
        strokeWidth = w * 0.026f,
        cap = StrokeCap.Round
    )
    // 새싹 잎 (좌우)
    val pivot = Offset(w * 0.5f, h * 0.08f)
    withTransform({ rotate(38f, pivot) }) {
        drawOval(
            color = LeafDark,
            topLeft = Offset(w * 0.26f, h * 0.055f),
            size = Size(w * 0.24f, h * 0.055f)
        )
    }
    withTransform({ rotate(-38f, pivot) }) {
        drawOval(
            color = LeafDark,
            topLeft = Offset(w * 0.50f, h * 0.055f),
            size = Size(w * 0.24f, h * 0.055f)
        )
    }

    // 몸통 (연두 → 초록 그라데이션)
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(BeanLight, BeanGreen),
            start = Offset(w * 0.15f, h * 0.2f),
            end = Offset(w * 0.85f, h * 0.9f)
        ),
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyW, bodyH)
    )
    // 몸통 하이라이트
    drawOval(
        color = Color.White.copy(alpha = 0.30f),
        topLeft = Offset(w * 0.13f, h * 0.22f),
        size = Size(w * 0.30f, h * 0.15f)
    )

    // 눈 (통통한 세로 타원 + 반짝임)
    val eyeColor = Color(0xFF33221C)
    listOf(0.35f, 0.65f).forEach { cx ->
        val center = Offset(w * cx, h * 0.48f)
        drawOval(
            color = eyeColor,
            topLeft = Offset(center.x - w * 0.042f, center.y - w * 0.058f),
            size = Size(w * 0.084f, w * 0.116f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.022f,
            center = Offset(center.x - w * 0.018f, center.y - w * 0.032f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.011f,
            center = Offset(center.x + w * 0.024f, center.y + w * 0.03f)
        )
    }

    // 볼터치
    listOf(0.22f, 0.78f).forEach { cx ->
        drawCircle(
            color = BubblePink.copy(alpha = 0.55f),
            radius = w * 0.048f,
            center = Offset(w * cx, h * 0.585f)
        )
    }

    // 미소
    drawArc(
        color = eyeColor,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.5f - w * 0.12f, h * 0.53f),
        size = Size(w * 0.24f, w * 0.24f),
        style = Stroke(width = w * 0.028f, cap = StrokeCap.Round)
    )
}

// ── 무지개 타이틀 ─────────────────────────────────────────

@Composable
fun RainbowTitle(
    text: String,
    fontSize: TextUnit = 58.sp,
    modifier: Modifier = Modifier
) {
    val charColors = listOf(
        Tangerine, Color(0xFFFFB300), BeanGreen, OceanBlue, GrapePurple, BubblePink
    )
    val rotations = listOf(-8f, 6f, -4f, 8f, -6f, 5f, -5f, 7f)

    Row(modifier = modifier) {
        text.forEachIndexed { i, c ->
            Text(
                text = c.toString(),
                color = charColors[i % charColors.size],
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF3E2723).copy(alpha = 0.25f),
                        offset = Offset(2f, 3f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.rotate(rotations[i % rotations.size])
            )
        }
    }
}

// ── 시작 버튼 ────────────────────────────────────────────

@Composable
fun PulsingStartButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.72f)
            .height(78.dp)
            .scale(pulse)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Tangerine.copy(alpha = 0.35f),
                spotColor = Tangerine.copy(alpha = 0.45f)
            )
            .background(SunnyYellow, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🚀 시작하기",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = InkBrown
        )
    }
}

// ── 배경 장식용 이모지 애니메이션 ──────────────────────────

@Composable
fun SwayingEmoji(
    text: String,
    fontSize: TextUnit,
    durationMillis: Int,
    modifier: Modifier = Modifier
) {
    val p by rememberInfiniteTransition(label = "sway")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "swayP"
        )
    Text(
        text = text,
        fontSize = fontSize,
        modifier = modifier.graphicsLayer {
            translationX = 14.dp.toPx() * p - 7.dp.toPx()
        }
    )
}

@Composable
fun BobbingEmoji(
    text: String,
    fontSize: TextUnit,
    durationMillis: Int,
    modifier: Modifier = Modifier
) {
    val p by rememberInfiniteTransition(label = "bob")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bobP"
        )
    Text(
        text = text,
        fontSize = fontSize,
        modifier = modifier.graphicsLayer {
            translationY = -8.dp.toPx() * p
        }
    )
}

@Composable
fun TwinklingEmoji(
    text: String,
    fontSize: TextUnit,
    durationMillis: Int,
    modifier: Modifier = Modifier
) {
    val p by rememberInfiniteTransition(label = "twinkle")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "twinkleP"
        )
    Text(
        text = text,
        fontSize = fontSize,
        modifier = modifier.graphicsLayer {
            alpha = 0.15f + 0.85f * p
            scaleX = 0.8f + 0.25f * p
            scaleY = 0.8f + 0.25f * p
        }
    )
}

@Composable
fun SwayingFlower(text: String, modifier: Modifier = Modifier) {
    val p by rememberInfiniteTransition(label = "flower")
        .animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flowerP"
        )
    Text(
        text = text,
        fontSize = 26.sp,
        modifier = modifier.rotate(p)
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
fun LandingScreenPreview() {
    VocaBeanTheme {
        LandingScreen()
    }
}
