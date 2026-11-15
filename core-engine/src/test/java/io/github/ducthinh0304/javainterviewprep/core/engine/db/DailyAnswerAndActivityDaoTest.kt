package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
class DailyAnswerAndActivityDaoTest {

    private lateinit var database: EngineDatabase
    private lateinit var answerDao: DailyAnswerDao
    private lateinit var activityDao: DailyActivityDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EngineDatabase::class.java
        ).build()
        answerDao = database.dailyAnswerDao()
        activityDao = database.dailyActivityDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun dailyAnswer_get_returnsNullWhenMissing() = runTest {
        assertNull(answerDao.get(LocalDate.of(2026, 11, 15)))
    }

    @Test
    fun dailyAnswer_upsert_isOneRowPerDate() = runTest {
        val date = LocalDate.of(2026, 11, 15)
        answerDao.upsert(DailyAnswerEntity(date = date, questionId = "q1", selectedOptionIndex = 1, wasCorrect = false, xpEarned = 0))
        answerDao.upsert(DailyAnswerEntity(date = date, questionId = "q1", selectedOptionIndex = 0, wasCorrect = true, xpEarned = 20))

        val stored = answerDao.get(date)
        assertEquals(true, stored?.wasCorrect)
        assertEquals(20, stored?.xpEarned)
    }

    @Test
    fun dailyActivity_upsert_accumulatesAcrossCalls() = runTest {
        val date = LocalDate.of(2026, 11, 15)
        activityDao.upsert(DailyActivityEntity(date = date, xpEarned = 10, questionsAnswered = 1))
        val existing = activityDao.get(date)!!
        activityDao.upsert(
            existing.copy(
                xpEarned = existing.xpEarned + 15,
                questionsAnswered = existing.questionsAnswered + 1
            )
        )

        val stored = activityDao.get(date)
        assertEquals(25, stored?.xpEarned)
        assertEquals(2, stored?.questionsAnswered)
    }
}
