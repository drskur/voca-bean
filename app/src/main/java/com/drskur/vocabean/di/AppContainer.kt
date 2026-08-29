package com.drskur.vocabean.di

import android.content.Context
import androidx.room.Room
import com.drskur.vocabean.data.db.VocaBeanDatabase
import com.drskur.vocabean.data.repository.RoomWordSetRepository
import com.drskur.vocabean.sound.AndroidSoundPlayer
import com.drskur.vocabean.sound.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 수동 의존성 컨테이너. 앱 프로세스 전체에서 공유하는 인스턴스를 보관한다.
 * (규모가 커지면 Hilt 등으로 교체)
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** DB 시딩, sets StateFlow 구독에 쓰는 애플리케이션 생명주기 스코프 */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: VocaBeanDatabase = Room.databaseBuilder(
        appContext,
        VocaBeanDatabase::class.java,
        VocaBeanDatabase.NAME
    ).build()

    val wordSetRepository: RoomWordSetRepository =
        RoomWordSetRepository(database.wordSetDao(), database, applicationScope)

    // ── 소리 효과 ──

    private val _soundEnabled = MutableStateFlow(!prefs.getBoolean(KEY_MUTED, false))

    /** 효과음 켜짐 여부 (보호자 설정에서 변경, 저장됨) */
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    val soundPlayer: SoundPlayer = AndroidSoundPlayer(appContext) { !_soundEnabled.value }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean(KEY_MUTED, !enabled).apply()
    }

    init {
        // 첫 실행 시에만 샘플 세트 시딩 (사용자가 전부 삭제해도 다시 생기지 않게)
        applicationScope.launch { maybeSeed() }
    }

    private suspend fun maybeSeed() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        wordSetRepository.seedIfEmpty()
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "vocabean_prefs"
        private const val KEY_SEEDED = "seeded_initial_sets"
        private const val KEY_MUTED = "sound_muted"
    }
}
