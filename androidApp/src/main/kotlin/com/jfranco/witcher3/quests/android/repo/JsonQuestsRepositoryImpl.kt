package com.jfranco.witcher3.quests.android.repo

import android.content.Context
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.witcher3.quests.android.R
import com.jfranco.witcher3.quests.android.persistence.dao.QuestStatusDao
import com.jfranco.witcher3.quests.android.persistence.entity.QuestStatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class JsonQuestsRepositoryImpl(
    private val context: Context,
    private val questStatusDao: QuestStatusDao,
) : QuestsRepository {

    override suspend fun getQuests(): List<Quest> {
        val inputStream = context.resources.openRawResource(R.raw.quests)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.use { it.readText() }
        return Json.decodeFromString<List<Quest>>(content)
    }

    override fun questsStatusUpdates(): Flow<List<QuestStatus>> {
        return questStatusDao.changes()
            .map { list -> list.map { QuestStatus(it.uid, it.isCompleted, it.isHidden) } }
    }

    override suspend fun save(questStatus: QuestStatus) {
        val entity = QuestStatusEntity(
            uid = questStatus.id,
            isCompleted = questStatus.isCompleted,
            isHidden = questStatus.isHidden
        )

        return questStatusDao.insertAll(entity)
    }

    override suspend fun getLastCompletedQuest(): QuestStatus? {
        return questStatusDao.lastCompleted()?.let {
            QuestStatus(it.uid, it.isCompleted, it.isHidden)
        }
    }
}