package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserStatsDaoTest {

    private lateinit var database: EngineDatabase
    private lateinit var dao: UserStatsDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        dao = database.userStatsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun get_returnsNullWhenMissing() = runTest {
        assertNull(dao.get())
    }

    @Test
    fun upsert_isAlwaysSingleRowAtId1() = runTest {
        dao.upsert(UserStatsEntity(totalXp = 10, currentStreak = 1, lastActiveDate = LocalDate.of(2026, 11, 14)))
        dao.upsert(UserStatsEntity(totalXp = 30, currentStreak = 2, lastActiveDate = LocalDate.of(2026, 11, 15)))

        val stored = dao.get()
        assertEquals(30, stored?.totalXp)
        assertEquals(2, stored?.currentStreak)
    }

    @Test
    fun observe_emitsLatestValueAfterUpsert() = runTest {
        dao.upsert(UserStatsEntity(totalXp = 5))

        val emitted = dao.observe().first()
        assertEquals(5, emitted?.totalXp)
    }
}
