package io.github.ducthinh0304.javainterviewprep.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;

    companion object {
        // Backwards compatibility mappings
        val JUNIOR: Difficulty get() = EASY
        val MID: Difficulty get() = MEDIUM
        val SENIOR: Difficulty get() = HARD
    }
}
