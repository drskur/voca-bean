package com.drskur.vocabean

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.drskur.vocabean.data.db.VocaBeanDatabase
import com.drskur.vocabean.data.model.Word
import com.drskur.vocabean.data.model.WordSet
import com.drskur.vocabean.data.repository.RoomWordSetRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Room 기반 저장소 검증. in-memory DB로 실제 CRUD/시딩/CASCADE를 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomWordSetRepositoryTest {

    private lateinit var db: VocaBeanDatabase
    private lateinit var repository: RoomWordSetRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VocaBeanDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomWordSetRepository(
            dao = db.wordSetDao(),
            db = db,
            scope = TestScope(UnconfinedTestDispatcher())
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    /** 시딩 완료 상태로 만든다 */
    private suspend fun seeded(): List<WordSet> {
        repository.seedIfEmpty()
        return repository.sets.first { it.isNotEmpty() }
    }

    @Test
    fun `빈 DB에서 시작하면 샘플 세트가 시딩된다`() = runTest {
        val sets = seeded()
        assertEquals(3, sets.size)
        assertTrue(sets.all { it.wordCount > 0 })
    }

    @Test
    fun `이미 데이터가 있으면 다시 시딩하지 않는다`() = runTest {
        seeded()
        repository.seedIfEmpty() // 중복 호출
        assertEquals(3, repository.sets.value.size)
    }

    @Test
    fun `새 세트를 저장하면 단어와 함께 조회된다`() = runTest {
        val newSet = WordSet(
            id = "vehicles",
            name = "탈것",
            emoji = "🚗",
            createdAt = 10_000L,
            words = listOf(
                Word("v1", "car", "자동차", "🚗"),
                Word("v2", "bus", "버스", "🚌")
            )
        )
        repository.upsertSet(newSet)

        val loaded = repository.getSet("vehicles")
        assertNotNull(loaded)
        assertEquals(2, loaded!!.wordCount)
        assertEquals(listOf("car", "bus"), loaded.words.map { it.english })
    }

    @Test
    fun `upsertSet은 단어 목록을 전체 동기화한다`() = runTest {
        val original = WordSet(
            id = "sync",
            name = "숫자",
            emoji = "🔢",
            createdAt = 20_000L,
            words = listOf(
                Word("n1", "one", "하나"),
                Word("n2", "two", "둘"),
                Word("n3", "three", "셋")
            )
        )
        repository.upsertSet(original)

        // n2 삭제, n1 수정, n4 추가
        val updated = original.copy(
            name = "숫자 놀이",
            words = listOf(
                Word("n1", "one", "하나 (1)"),
                Word("n3", "three", "셋"),
                Word("n4", "four", "넷")
            )
        )
        repository.upsertSet(updated)

        val loaded = repository.getSet("sync")!!
        assertEquals("숫자 놀이", loaded.name)
        assertEquals(setOf("n1", "n3", "n4"), loaded.words.map { it.id }.toSet())
        assertEquals("하나 (1)", loaded.words.first { it.id == "n1" }.korean)
    }

    @Test
    fun `세트를 삭제하면 단어도 함께 삭제된다`() = runTest {
        repository.upsertSet(
            WordSet(
                id = "gone",
                name = "지워질 세트",
                emoji = "💨",
                createdAt = 30_000L,
                words = listOf(Word("g1", "bye", "안녕"))
            )
        )
        repository.deleteSet("gone")

        assertNull(repository.getSet("gone"))
        assertNull(db.wordSetDao().getWord("g1")) // CASCADE 확인
    }

    @Test
    fun `단어를 개별 추가, 수정, 삭제할 수 있다`() = runTest {
        repository.upsertSet(
            WordSet(id = "words", name = "테스트", emoji = "🧪", createdAt = 40_000L)
        )

        repository.addWord("words", Word("w1", "dog", "강아지", "🐶"))
        assertEquals(1, repository.getSet("words")?.wordCount)

        repository.updateWord("words", Word("w1", "dog", "멍멍이", "🐶"))
        assertEquals("멍멍이", repository.getSet("words")?.words?.first()?.korean)

        repository.deleteWord("words", "w1")
        assertEquals(0, repository.getSet("words")?.wordCount)
    }

    @Test
    fun `sets 플로우는 변경 사항을 반영한다`() = runTest {
        seeded()
        val before = repository.sets.value.size

        repository.upsertSet(
            WordSet(id = "flow", name = "플로우", emoji = "🌊", createdAt = 50_000L)
        )

        val after = repository.sets.first { it.size == before + 1 }
        assertEquals("플로우", after.last().name)
    }

    @Test
    fun `세트 목록은 생성 순으로 정렬된다`() = runTest {
        val sets = seeded()
        assertEquals(listOf("동물 친구들", "맛있는 과일", "알록달록 색깔"), sets.map { it.name })
    }
}
