package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DevLevel(
    val minXp: Int,
    val title: String,
    val badgeLabel: String,
    val description: String
) {
    SNAPSHOT(0, "SNAPSHOT", "dev-snapshot", "Experimental build. Establishing foundations."),
    ALPHA(100, "ALPHA", "v0.1.0-alpha", "Core features taking shape. Testing runtime behaviors."),
    BETA(300, "BETA", "v0.5.0-beta", "API stabilized. Refining edge cases and concurrency."),
    RC(700, "RC", "v1.0.0-rc", "Release Candidate. Production-readiness verified."),
    GA(1500, "GA", "v1.0.0-ga", "General Availability. Rock-solid mastery deployed."),
    LTS(3000, "LTS", "v21-lts", "Long Term Support. Battle-tested architecture and deep JVM internals.");

    val nextLevel: DevLevel?
        get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun fromXp(xp: Int): DevLevel {
            return entries.lastOrNull { xp >= it.minXp } ?: SNAPSHOT
        }
    }
}

@Serializable
data class UserStats(
    val xp: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val comboMultiplier: Double = 1.0,
    val consecutiveCorrectAnswers: Int = 0,
    val lastActiveEpochDay: Long? = null,
    val totalExercisesAnswered: Int = 0,
    val totalCorrectAnswers: Int = 0
) {
    val level: DevLevel
        get() = DevLevel.fromXp(xp)

    val xpToNextLevel: Int?
        get() = level.nextLevel?.minXp?.minus(xp)
}
