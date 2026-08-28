package com.drskur.vocabean.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.repository.WordSetRepository
import com.drskur.vocabean.ui.common.CircleBackButton
import com.drskur.vocabean.ui.common.noRippleClickable
import com.drskur.vocabean.ui.theme.BeanGreen
import com.drskur.vocabean.ui.theme.BeanLight
import com.drskur.vocabean.ui.theme.CreamBottom
import com.drskur.vocabean.ui.theme.InkBrown
import com.drskur.vocabean.ui.theme.SetPalette

/** 세트 아이콘으로 쓸 이모지 프리셋 */
private val SetEmojiPresets = listOf(
    "🐶", "🐱", "🐰", "🐻", "🐼", "🦊", "🐸", "🐵",
    "🍎", "🍌", "🍓", "🍉", "🍇", "🥕", "🍞", "🧸",
    "🌈", "⭐", "🎈", "🚗", "✈️", "🚀", "⚽", "📚"
)

/** 세트 생성/편집 화면 */
@Composable
fun SetEditorScreen(
    repository: WordSetRepository,
    setId: String?,
    onDone: () -> Unit = {}
) {
    val viewModel: SetEditorViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SetEditorViewModel(repository, setId) }
        }
    )
    val state by viewModel.state.collectAsState()

    // 단어 입력 폼 상태
    var english by remember { mutableStateOf("") }
    var korean by remember { mutableStateOf("") }
    var wordEmoji by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBottom)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                CircleBackButton(onClick = onDone)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (state.isNew) "새 세트 만들기" else "세트 편집",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBrown
                )
            }

            if (!state.loaded) {
                // 로딩 중
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌱", fontSize = 40.sp)
                }
            } else {
                // 세트 이름
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::setName,
                    label = { Text("세트 이름") },
                    placeholder = { Text("예: 동물 친구들") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(22.dp))

                // 아이콘 선택
                SectionTitle("아이콘 고르기")
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SetEmojiPresets) { emoji ->
                        EmojiChip(
                            emoji = emoji,
                            selected = state.emoji == emoji,
                            onClick = { viewModel.setEmoji(emoji) }
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                // 색깔 선택
                SectionTitle("색깔 고르기")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SetPalette.forEachIndexed { index, color ->
                        ColorDot(
                            color = color,
                            selected = index == state.colorIndex,
                            onClick = { viewModel.setColor(index) }
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                // 단어 목록
                SectionTitle("단어 목록 (${state.words.size}개)")
                Spacer(Modifier.height(12.dp))

                // 단어 추가 폼
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = english,
                        onValueChange = { english = it },
                        label = { Text("영어") },
                        placeholder = { Text("dog") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = korean,
                        onValueChange = { korean = it },
                        label = { Text("뜻") },
                        placeholder = { Text("강아지") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = wordEmoji,
                        onValueChange = { wordEmoji = it },
                        label = { Text("이모지 (선택)") },
                        placeholder = { Text("🐶") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    AddWordButton(
                        enabled = english.isNotBlank() && korean.isNotBlank(),
                        onClick = {
                            if (viewModel.addWord(english, korean, wordEmoji)) {
                                english = ""
                                korean = ""
                                wordEmoji = ""
                            }
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))

                if (state.words.isEmpty()) {
                    Text(
                        text = "첫 단어를 추가해보세요!  예) dog - 강아지 🐶",
                        fontSize = 14.sp,
                        color = InkBrown.copy(alpha = 0.55f)
                    )
                } else {
                    state.words.forEach { word ->
                        WordRow(
                            word = word,
                            onDelete = { viewModel.removeWord(word.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // 하단 저장 버튼 공간
                Spacer(Modifier.height(110.dp))
            }
        }

        // 저장 버튼 (하단 고정)
        if (state.loaded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(CreamBottom)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                val canSave = state.name.isNotBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (canSave) BeanGreen else BeanGreen.copy(alpha = 0.35f))
                        .noRippleClickable(onClick = { if (canSave) viewModel.save(onDone) })
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💾 저장하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = InkBrown
    )
}

@Composable
private fun EmojiChip(emoji: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(if (selected) BeanLight else Color.White)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) BeanGreen else Color(0x14000000),
                shape = shape
            )
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) BeanGreen else Color(0x22000000),
                shape = CircleShape
            )
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Text(
                text = "✓",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = InkBrown
            )
        }
    }
}

@Composable
private fun AddWordButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) BeanGreen else BeanGreen.copy(alpha = 0.3f))
            .noRippleClickable(onClick = { if (enabled) onClick() })
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "추가",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
private fun WordRow(word: Word, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(26.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = word.emoji.orEmpty(), fontSize = 18.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = word.english,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "—",
            fontSize = 14.sp,
            color = InkBrown.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = word.korean,
            fontSize = 16.sp,
            color = InkBrown
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "✕",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown.copy(alpha = 0.45f),
            modifier = Modifier
                .clip(CircleShape)
                .noRippleClickable(onClick = onDelete)
                .padding(6.dp)
        )
    }
}
