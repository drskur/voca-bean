package com.drskur.vocabean.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.drskur.vocabean.data.model.QuizMode
import com.drskur.vocabean.data.model.QuizQuestion
import com.drskur.vocabean.data.model.QuizType
import com.drskur.vocabean.data.repository.InMemoryWordSetRepository
import com.drskur.vocabean.data.repository.SampleData
import com.drskur.vocabean.data.repository.WordSetRepository
import com.drskur.vocabean.sound.LocalSoundPlayer
import com.drskur.vocabean.sound.SoundEffect
import com.drskur.vocabean.ui.common.CircleBackButton
import com.drskur.vocabean.ui.common.MaxContentWidth
import com.drskur.vocabean.ui.common.noRippleClickable
import com.drskur.vocabean.ui.theme.BeanGreen
import com.drskur.vocabean.ui.theme.CreamBottom
import com.drskur.vocabean.ui.theme.InkBrown
import com.drskur.vocabean.ui.theme.SkyMiddle
import com.drskur.vocabean.ui.theme.SkyTop
import com.drskur.vocabean.ui.theme.VocaBeanTheme

/**
 * 퀴즈 화면.
 * 모드 선택(뜻 맞히기 / 영어 쓰기 / 섞어 내기) → 1문제씩 주관식 풀이 → 결과.
 */
@Composable
fun QuizScreen(
    repository: WordSetRepository,
    setId: String,
    onBackClick: () -> Unit = {}
) {
    val soundPlayer = LocalSoundPlayer.current
    val viewModel: QuizViewModel = viewModel(
        factory = viewModelFactory {
            initializer { QuizViewModel(repository, soundPlayer, setId) }
        }
    )
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyMiddle, CreamBottom))),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            !state.loaded -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "🌱", fontSize = 40.sp)
                }
            }

            state.wordCount == 0 -> {
                EmptyQuiz(onBackClick = onBackClick)
            }

            state.playing != null -> {
                PlayingContent(
                    playing = state.playing!!,
                    setEmoji = state.setEmoji,
                    onBackClick = onBackClick,
                    onInputChanged = viewModel::onInputChanged,
                    onSubmit = viewModel::submit,
                    onNext = viewModel::next
                )
            }

            state.finished != null -> {
                FinishedContent(
                    finished = state.finished!!,
                    setEmoji = state.setEmoji,
                    onBackClick = onBackClick,
                    onRetry = viewModel::retry,
                    onModeSelect = viewModel::backToModeSelect
                )
            }

            else -> {
                ModeSelectContent(
                    setName = state.setName,
                    setEmoji = state.setEmoji,
                    onBackClick = onBackClick,
                    onStart = viewModel::start
                )
            }
        }
    }
}

// ── 모드 선택 ────────────────────────────────────────────

@Composable
private fun ModeSelectContent(
    setName: String,
    setEmoji: String,
    onBackClick: () -> Unit,
    onStart: (QuizMode) -> Unit
) {
    val sound = LocalSoundPlayer.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = MaxContentWidth)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            CircleBackButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onBackClick
            )
            Text(
                text = "$setEmoji $setName",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "어떻게 풀어볼까요?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = InkBrown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                ModeCard(
                    emoji = "🇰🇷",
                    title = "뜻 맞히기",
                    description = "영어를 보고 한국말을 써요",
                    background = Color(0xFFFFD6E0),
                    onClick = {
                        sound.play(SoundEffect.TAP)
                        onStart(QuizMode.MEANING)
                    }
                )
                ModeCard(
                    emoji = "🔤",
                    title = "영어 쓰기",
                    description = "한국말을 보고 영어를 써요",
                    background = Color(0xFFC3E5FF),
                    onClick = {
                        sound.play(SoundEffect.TAP)
                        onStart(QuizMode.ENGLISH)
                    }
                )
                ModeCard(
                    emoji = "🎲",
                    title = "섞어 내기",
                    description = "두 가지가 골고루 나와요",
                    background = Color(0xFFFFF1B8),
                    onClick = {
                        sound.play(SoundEffect.TAP)
                        onStart(QuizMode.MIXED)
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    emoji: String,
    title: String,
    description: String,
    background: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .noRippleClickable(onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = InkBrown
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = InkBrown.copy(alpha = 0.7f)
            )
        }
    }
}

// ── 풀이 중 ──────────────────────────────────────────────

@Composable
private fun PlayingContent(
    playing: QuizUiState.PlayingState,
    setEmoji: String,
    onBackClick: () -> Unit,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val question = playing.question
    val sound = LocalSoundPlayer.current
    val focusRequester = remember { FocusRequester() }

    // 문제가 바뀔 때마다 입력창에 포커스 (키보드 유지)
    LaunchedEffect(playing.index) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = MaxContentWidth)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // 상단: 뒤로가기 + 진행 상황
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircleBackButton(onClick = onBackClick)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "$setEmoji ${playing.index + 1} / ${playing.questions.size}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown
            )
        }

        Spacer(Modifier.height(14.dp))

        // 진행 막대
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.7f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(playing.progress)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BeanGreen)
            )
        }

        // 키보드가 올라오면 먼저 줄어들어 문제·입력창·버튼이 항상 보이게 한다
        Spacer(Modifier.weight(1f))

        // 문제 카드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (question.type) {
                    QuizType.ENGLISH_TO_KOREAN -> "이 영어 단어의 뜻은 무엇일까요?"
                    QuizType.KOREAN_TO_ENGLISH -> "이 뜻은 어떤 영어 단어일까요?"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkBrown.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))
            if (question.word.emoji != null) {
                Text(text = question.word.emoji, fontSize = 44.sp)
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = question.prompt,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = InkBrown,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(14.dp))

        // 피드백 영역 (높이 고정)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            when (playing.feedback) {
                null -> Text(
                    text = "정답을 쓰고 확인을 눌러요! ✏️",
                    fontSize = 14.sp,
                    color = InkBrown.copy(alpha = 0.55f)
                )

                QuizUiState.Feedback.CORRECT -> Text(
                    text = "🎉 맞았어요!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BeanGreen
                )

                QuizUiState.Feedback.WRONG -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "💪 아쉬워요!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE5484D)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "정답: ${question.answerDisplay}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBrown
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // 정답 입력
        OutlinedTextField(
            value = playing.input,
            onValueChange = onInputChanged,
            readOnly = playing.feedback != null,
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            textStyle = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = InkBrown
            ),
            placeholder = {
                Text(
                    text = "여기에 써요",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (playing.feedback == null) onSubmit() else onNext() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Spacer(Modifier.height(18.dp))

        // 확인 / 다음 버튼
        val isAnswered = playing.feedback != null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(BeanGreen)
                .noRippleClickable(onClick = {
                    sound.play(SoundEffect.TAP)
                    if (isAnswered) onNext() else onSubmit()
                })
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    !isAnswered -> "확인!"
                    !playing.isLast -> "다음 ➡️"
                    else -> "결과 보기 🏁"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

// ── 결과 ─────────────────────────────────────────────────

@Composable
private fun FinishedContent(
    finished: QuizUiState.FinishedState,
    setEmoji: String,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onModeSelect: () -> Unit
) {
    val sound = LocalSoundPlayer.current
    val ratio = if (finished.total == 0) 0f else finished.correct.toFloat() / finished.total
    val resultEmoji = when {
        ratio >= 0.999f -> "🏆"
        ratio >= 0.7f -> "🎉"
        ratio >= 0.4f -> "💪"
        else -> "🌱"
    }
    val message = when {
        ratio >= 0.999f -> "와! 전부 맞혔어요!"
        ratio >= 0.7f -> "정말 잘했어요!"
        ratio >= 0.4f -> "잘했어요! 조금만 더!"
        else -> "괜찮아요! 다시 해봐요!"
    }
    val stars = when {
        ratio >= 0.999f -> 3
        ratio >= 0.7f -> 2
        ratio >= 0.4f -> 1
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = MaxContentWidth)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            CircleBackButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onBackClick
            )
            Text(
                text = "$setEmoji $finished.correct / ${finished.total}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = resultEmoji, fontSize = 80.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "${finished.total}문제 중 ${finished.correct}문제 맞혔어요!",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = InkBrown
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "⭐".repeat(stars) + "☆".repeat(3 - stars),
                    fontSize = 30.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkBrown.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(BeanGreen)
                        .noRippleClickable(onClick = {
                            sound.play(SoundEffect.TAP)
                            onRetry()
                        })
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "한 번 더! 🔄",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .noRippleClickable(onClick = onModeSelect)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "다른 방법으로 풀기 🎮",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkBrown
                    )
                }
            }
        }
    }
}

// ── 빈 세트 ──────────────────────────────────────────────

@Composable
private fun EmptyQuiz(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = MaxContentWidth)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        CircleBackButton(onClick = onBackClick)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🍃", fontSize = 48.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "이 세트에는 아직 단어가 없어요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkBrown
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "보호자 설정에서 단어를 추가해주세요!",
                    fontSize = 14.sp,
                    color = InkBrown.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
fun QuizModeSelectPreview() {
    VocaBeanTheme {
        QuizScreen(
            repository = InMemoryWordSetRepository(SampleData.sets),
            setId = SampleData.sets.first().id
        )
    }
}
