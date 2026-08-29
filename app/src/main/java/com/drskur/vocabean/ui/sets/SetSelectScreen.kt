package com.drskur.vocabean.ui.sets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.InMemoryWordSetRepository
import com.drskur.vocabean.data.repository.SampleData
import com.drskur.vocabean.data.repository.WordSetRepository
import com.drskur.vocabean.sound.LocalSoundPlayer
import com.drskur.vocabean.sound.SoundEffect
import com.drskur.vocabean.ui.common.CircleBackButton
import com.drskur.vocabean.ui.common.MaxContentWidth
import com.drskur.vocabean.ui.common.noRippleClickable
import com.drskur.vocabean.ui.theme.CreamBottom
import com.drskur.vocabean.ui.theme.InkBrown
import com.drskur.vocabean.ui.theme.SkyMiddle
import com.drskur.vocabean.ui.theme.SkyTop
import com.drskur.vocabean.ui.theme.VocaBeanTheme
import com.drskur.vocabean.ui.theme.setPaletteColor

/** 아이용 세트 선택 화면: 카드를 고르면 해당 세트의 퀴즈로 이동한다. */
@Composable
fun SetSelectScreen(
    repository: WordSetRepository,
    onBackClick: () -> Unit = {},
    onSetClick: (String) -> Unit = {}
) {
    val sets by repository.sets.collectAsState()
    val sound = LocalSoundPlayer.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyMiddle, CreamBottom))),
        contentAlignment = Alignment.TopCenter
    ) {
        // 언덕
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Color(0xFFB8EC9E),
                    RoundedCornerShape(topStart = 70.dp, topEnd = 70.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = MaxContentWidth)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // 헤더
            Box(modifier = Modifier.fillMaxWidth()) {
                CircleBackButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = onBackClick
                )
                Text(
                    text = "어떤 세트로 놀까요?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = InkBrown,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "마음에 드는 세트를 골라보세요!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkBrown.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(18.dp))

            if (sets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🌱", fontSize = 48.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "아직 세트가 없어요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBrown
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "보호자 설정에서 만들어주세요!",
                            fontSize = 14.sp,
                            color = InkBrown.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sets, key = { it.id }) { set ->
                        SetCard(set = set, onClick = {
                            sound.play(SoundEffect.TAP)
                            onSetClick(set.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun SetCard(set: WordSet, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(setPaletteColor(set.colorIndex))
            .noRippleClickable(onClick)
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = set.emoji, fontSize = 44.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = set.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = InkBrown
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "단어 ${set.wordCount}개",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = InkBrown.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
fun SetSelectScreenPreview() {
    VocaBeanTheme {
        SetSelectScreen(repository = InMemoryWordSetRepository(SampleData.sets))
    }
}
