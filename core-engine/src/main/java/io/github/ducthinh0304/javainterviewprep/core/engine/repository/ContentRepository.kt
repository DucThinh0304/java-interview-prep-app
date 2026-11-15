package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.ducthinh0304.javainterviewprep.core.model.Pack
import io.github.ducthinh0304.javainterviewprep.core.model.PackValidator
import io.github.ducthinh0304.javainterviewprep.core.model.Question
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
    private var cachedPack: Pack? = null

    suspend fun getPack(packId: String = "java"): Pack = withContext(Dispatchers.IO) {
        cachedPack?.let { return@withContext it }

        val assetManager = context.assets
        val packFilePath = "packs/$packId/pack.json"

        val packJsonString = assetManager.open(packFilePath).use {
            InputStreamReader(it).readText()
        }

        val pack = json.decodeFromString<Pack>(packJsonString)
        PackValidator.validateOrThrow(pack)

        cachedPack = pack
        pack
    }

    suspend fun getDailyQuestion(epochDay: Long, packId: String = "java"): Question? {
        val pack = getPack(packId)
        val allQuestions = pack.allQuestions()
        if (allQuestions.isEmpty()) return null
        val index = (epochDay.mod(allQuestions.size.toLong())).toInt()
        return allQuestions[index]
    }
}
