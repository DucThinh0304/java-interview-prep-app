package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.ducthinh0304.javainterviewprep.core.model.ContentPack
import io.github.ducthinh0304.javainterviewprep.core.model.Exercise
import io.github.ducthinh0304.javainterviewprep.core.model.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json
) {
    private var cachedPack: ContentPack? = null

    suspend fun getPack(packId: String = "java"): ContentPack = withContext(Dispatchers.IO) {
        cachedPack?.let { return@withContext it }

        val assetManager = context.assets
        val basePath = "packs/$packId"

        val manifestString = assetManager.open("$basePath/manifest.json").use {
            InputStreamReader(it).readText()
        }
        val questionsString = assetManager.open("$basePath/questions.json").use {
            InputStreamReader(it).readText()
        }
        val lessonsString = assetManager.open("$basePath/lessons.json").use {
            InputStreamReader(it).readText()
        }

        val questions: List<Exercise> = json.decodeFromString(questionsString)
        val lessons: List<Lesson> = json.decodeFromString(lessonsString)

        val pack = ContentPack(
            id = packId,
            title = "Java Core & JVM Internals",
            version = 1,
            description = "Developer-grade Java interview drills",
            questions = questions,
            lessons = lessons
        )

        cachedPack = pack
        pack
    }

    suspend fun getDailyQuestion(epochDay: Long, packId: String = "java"): Exercise? {
        val pack = getPack(packId)
        if (pack.questions.isEmpty()) return null
        val index = (epochDay.mod(pack.questions.size.toLong())).toInt()
        return pack.questions[index]
    }
}
