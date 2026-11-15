package io.github.ducthinh0304.javainterviewprep.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationModelTest {

    @Test
    fun devLevel_thresholdsMapCorrectly() {
        assertEquals(DevLevel.SNAPSHOT, DevLevel.fromXp(0))
        assertEquals(DevLevel.SNAPSHOT, DevLevel.fromXp(99))
        assertEquals(DevLevel.ALPHA, DevLevel.fromXp(100))
        assertEquals(DevLevel.BETA, DevLevel.fromXp(300))
        assertEquals(DevLevel.RC, DevLevel.fromXp(700))
        assertEquals(DevLevel.GA, DevLevel.fromXp(1500))
        assertEquals(DevLevel.LTS, DevLevel.fromXp(3000))
        assertEquals(DevLevel.LTS, DevLevel.fromXp(9999))
    }

    @Test
    fun userStats_nextLevelCalculation() {
        val stats = UserStats(xp = 50)
        assertEquals(DevLevel.SNAPSHOT, stats.level)
        assertEquals(50, stats.xpToNextLevel) // 100 - 50 = 50
    }
}
