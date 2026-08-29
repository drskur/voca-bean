package com.drskur.vocabean.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.WordSetRepository
import com.drskur.vocabean.ui.common.CircleBackButton
import com.drskur.vocabean.ui.common.MaxContentWidth
import com.drskur.vocabean.ui.common.noRippleClickable
import com.drskur.vocabean.ui.theme.BeanGreen
import com.drskur.vocabean.ui.theme.CreamBottom
import com.drskur.vocabean.ui.theme.InkBrown
import com.drskur.vocabean.ui.theme.setPaletteColor

/** 보호자 설정 홈: 단어 세트 목록 관리 */
@Composable
fun ParentHomeScreen(
    repository: WordSetRepository,
    soundEnabled: Boolean = true,
    onSoundEnabledChange: (Boolean) -> Unit = {},
    onBackClick: () -> Unit = {},
    onAddSet: () -> Unit = {},
    onEditSet: (String) -> Unit = {}
) {
    val viewModel: ParentHomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ParentHomeViewModel(repository) }
        }
    )
    val sets by viewModel.sets.collectAsState()
    var deleteTarget by remember { mutableStateOf<WordSet?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBottom)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = MaxContentWidth)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            CircleBackButton(onClick = onBackClick)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "단어 세트 관리",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown
            )
        }

        // 소리 효과 토글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🔊 소리 효과",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkBrown
            )
            Spacer(Modifier.weight(1f))
            Switch(
                checked = soundEnabled,
                onCheckedChange = onSoundEnabledChange
            )
        }

        // 세트 목록
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = MaxContentWidth),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sets.isEmpty()) {
                item { EmptyState() }
            }
            items(sets, key = { it.id }) { set ->
                SetRow(
                    set = set,
                    onEdit = { onEditSet(set.id) },
                    onDelete = { deleteTarget = set }
                )
            }
        }

        // 새 세트 만들기 버튼
        Box(Modifier.padding(horizontal = 20.dp, vertical = 14.dp).widthIn(max = 480.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(BeanGreen)
                    .noRippleClickable(onClick = onAddSet)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "＋ 새 세트 만들기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }

    // 삭제 확인 다이얼로그
    deleteTarget?.let { set ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("세트 삭제") },
            text = {
                Text("\"${set.name}\" 세트와 단어 ${set.wordCount}개가 모두 지워져요.\n정말 삭제할까요?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSet(set.id)
                        deleteTarget = null
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🌱", fontSize = 44.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            text = "아직 세트가 없어요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkBrown
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "아래 버튼으로 첫 세트를 만들어보세요!",
            fontSize = 14.sp,
            color = InkBrown.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SetRow(set: WordSet, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .noRippleClickable(onClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(setPaletteColor(set.colorIndex)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = set.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = set.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "단어 ${set.wordCount}개",
                fontSize = 13.sp,
                color = InkBrown.copy(alpha = 0.6f)
            )
        }

        ActionButton(emoji = "✏️", onClick = onEdit)
        ActionButton(emoji = "🗑️", onClick = onDelete)
    }
}

@Composable
private fun ActionButton(emoji: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 18.sp)
    }
}
