package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.ducthinh0304.javainterviewprep.core.model.Pack
import io.github.ducthinh0304.javainterviewprep.core.model.PackValidator
import io.github.ducthinh0304.javainterviewprep.core.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val mutex = Mutex()
    private val cachedPacks = mutableMapOf<String, Pack>()

    suspend fun getPack(packId: String = DEFAULT_PACK_ID): Pack {
        cachedPacks[packId]?.let { return it }
        return mutex.withLock {
            cachedPacks.getOrPut(packId) { loadPack(packId) }
        }
    }

    suspend fun getDailyQuestion(epochDay: Long, packId: String = DEFAULT_PACK_ID): Question? {
        val pack = getPack(packId)
        val allQuestions = pack.allQuestions()
        if (allQuestions.isEmpty()) return null
        val index = (epochDay.mod(allQuestions.size.toLong())).toInt()
        return allQuestions[index]
    }

    private suspend fun loadPack(packId: String): Pack = withContext(Dispatchers.IO) {
        val packFilePath = "packs/$packId/pack.json"
        val packJsonString = context.assets.open(packFilePath).use {
            InputStreamReader(it).readText()
        }

        val pack = json.decodeFromString<Pack>(packJsonString)
        PackValidator.validateOrThrow(pack)
        pack
    }

    companion object {
        const val DEFAULT_PACK_ID = "java"
    }
}
