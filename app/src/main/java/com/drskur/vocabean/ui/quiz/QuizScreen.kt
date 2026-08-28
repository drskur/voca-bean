package com.drskur.vocabean.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drskur.vocabean.data.repository.InMemoryWordSetRepository
import com.drskur.vocabean.data.repository.SampleData
import com.drskur.vocabean.data.repository.WordSetRepository
import com.drskur.vocabean.ui.common.CircleBackButton
import com.drskur.vocabean.ui.theme.CreamBottom
import com.drskur.vocabean.ui.theme.InkBrown
import com.drskur.vocabean.ui.theme.SkyMiddle
import com.drskur.vocabean.ui.theme.SkyTop
import com.drskur.vocabean.ui.theme.VocaBeanTheme

/**
 * 퀴즈 화면. 다음 단계에서 QuizGenerator와 연결해 실제 퀴즈를 푸는 UI를 붙인다.
 * 지금은 선택한 세트 정보를 보여주는 자리만 마련해둔다.
 */
@Composable
fun QuizScreen(
    repository: WordSetRepository,
    setId: String,
    onBackClick: () -> Unit = {}
) {
    val sets by repository.sets.collectAsState()
    val set = sets.find { it.id == setId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyMiddle, CreamBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                CircleBackButton(onClick = onBackClick)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = set?.emoji ?: "🌱", fontSize = 64.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = set?.name ?: "퀴즈",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBrown
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(text = "🚧", fontSize = 52.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "퀴즈 준비 중!",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = InkBrown
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "곧 재미있는 퀴즈로 만나요!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkBrown.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
fun QuizScreenPreview() {
    VocaBeanTheme {
        QuizScreen(
            repository = InMemoryWordSetRepository(SampleData.sets),
            setId = SampleData.sets.first().id
        )
    }
}
