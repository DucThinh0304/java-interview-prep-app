package io.github.ducthinh0304.javainterviewprep.core.engine.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.ducthinh0304.javainterviewprep.core.engine.db.DailyAnswerEntity
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

    /**
     * The daily is picked only from single-pick kinds: [DailyAnswerEntity] stores one option index,
     * so other kinds could never be answered or checked in.
     */
    suspend fun getDailyQuestion(epochDay: Long, packId: String = DEFAULT_PACK_ID): Question? {
        val candidates = getPack(packId).allQuestions()
            .filter { it is Question.SingleChoice || it is Question.PredictOutput }
        if (candidates.isEmpty()) return null
        return candidates[epochDay.mod(candidates.size.toLong()).toInt()]
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
