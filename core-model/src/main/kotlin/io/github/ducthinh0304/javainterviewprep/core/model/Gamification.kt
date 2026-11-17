package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

/** XP thresholds live in the engine's LevelLadder config, not here. */
@Serializable
enum class DevLevel(
    val title: String,
    val badgeLabel: String,
    val description: String
) {
    SNAPSHOT("SNAPSHOT", "dev-snapshot", "Experimental build. Establishing foundations."),
    ALPHA("ALPHA", "v0.1.0-alpha", "Core features taking shape. Testing runtime behaviors."),
    BETA("BETA", "v0.5.0-beta", "API stabilized. Refining edge cases and concurrency."),
    RC("RC", "v1.0.0-rc", "Release Candidate. Production-readiness verified."),
    GA("GA", "v1.0.0-ga", "General Availability. Rock-solid mastery deployed."),
    LTS("LTS", "v21-lts", "Long Term Support. Battle-tested architecture and deep JVM internals.")
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
)
