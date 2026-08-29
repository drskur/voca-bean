package com.drskur.vocabean.sound

import android.content.Context
import android.media.SoundPool
import androidx.compose.runtime.compositionLocalOf
import com.drskur.vocabean.R

/** 효과음 종류 */
enum class SoundEffect {
    /** 버튼/카드 탭 */
    TAP,

    /** 정답 */
    CORRECT,

    /** 오답 */
    WRONG,

    /** 퀴즈 완료 팡파레 */
    COMPLETE
}

/** 효과음 재생기. ViewModel 주입·테스트를 위한 추상화. */
interface SoundPlayer {
    fun play(effect: SoundEffect)
}

/** 무음 구현 (테스트·프리뷰용) */
object SilentSoundPlayer : SoundPlayer {
    override fun play(effect: SoundEffect) = Unit
}

/**
 * SoundPool 기반 효과음 재생기.
 * 짧은 효과음을 저지연으로 재생하며, [isMuted]가 true면 재생하지 않는다.
 */
class AndroidSoundPlayer(
    context: Context,
    private val isMuted: () -> Boolean
) : SoundPlayer {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .build()

    private val loaded = mutableSetOf<Int>()

    private val ids: Map<SoundEffect, Int> = mapOf(
        SoundEffect.TAP to pool.load(context, R.raw.sfx_tap, 1),
        SoundEffect.CORRECT to pool.load(context, R.raw.sfx_correct, 1),
        SoundEffect.WRONG to pool.load(context, R.raw.sfx_wrong, 1),
        SoundEffect.COMPLETE to pool.load(context, R.raw.sfx_complete, 1)
    )

    init {
        pool.setOnLoadCompleteListener { _, sampleId, _ -> loaded.add(sampleId) }
    }

    override fun play(effect: SoundEffect) {
        if (isMuted()) return
        val id = ids[effect] ?: return
        if (id !in loaded) return // 아직 로딩 전이면 건너뜀
        pool.play(id, VOLUME, VOLUME, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }

    companion object {
        private const val MAX_STREAMS = 4
        private const val VOLUME = 0.9f
    }
}

/** 화면 어디서나 효과음을 재생할 수 있게 제공하는 CompositionLocal */
val LocalSoundPlayer = compositionLocalOf<SoundPlayer> { SilentSoundPlayer }
