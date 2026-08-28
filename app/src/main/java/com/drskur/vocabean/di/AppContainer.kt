package com.drskur.vocabean.di

import android.content.Context
import androidx.room.Room
import com.drskur.vocabean.data.db.VocaBeanDatabase
import com.drskur.vocabean.data.repository.RoomWordSetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 수동 의존성 컨테이너. 앱 프로세스 전체에서 공유하는 인스턴스를 보관한다.
 * (규모가 커지면 Hilt 등으로 교체)
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** DB 시딩, sets StateFlow 구독에 쓰는 애플리케이션 생명주기 스코프 */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: VocaBeanDatabase = Room.databaseBuilder(
        appContext,
        VocaBeanDatabase::class.java,
        VocaBeanDatabase.NAME
    ).build()

    val wordSetRepository: RoomWordSetRepository =
        RoomWordSetRepository(database.wordSetDao(), database, applicationScope)

    init {
        // 첫 실행 시에만 샘플 세트 시딩 (사용자가 전부 삭제해도 다시 생기지 않게)
        applicationScope.launch { maybeSeed() }
    }

    private suspend fun maybeSeed() {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        wordSetRepository.seedIfEmpty()
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "vocabean_prefs"
        private const val KEY_SEEDED = "seeded_initial_sets"
    }
}
