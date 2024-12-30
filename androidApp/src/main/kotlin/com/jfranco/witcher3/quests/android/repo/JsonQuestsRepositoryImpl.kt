package com.jfranco.witcher3.quests.android.repo

import com.jfranco.w3.quests.shared.AppQuestsRepository
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestStatus

import com.jfranco.witcher3.quests.android.persistence.dao.QuestStatusDao
import com.jfranco.witcher3.quests.android.persistence.entity.QuestStatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class JsonQuestsRepositoryImpl(
    private val content: InputStream,
    private val questStatusDao: QuestStatusDao,
) : AppQuestsRepository {

    private val questsByLocation: List<Pair<String, List<Quest>>> by lazy {
        val reader = BufferedReader(InputStreamReader(content))
        val content = reader.use { it.readText() }
        val quests = Json.decodeFromString<List<Quest>>(content)
        quests.groupContiguousItems()
    }


    override fun updates(): Flow<List<Pair<String, List<Quest>>>> {
        return questStatusDao.changes().map { list ->
            list.associateBy { it.uid }
        }.map { questsStatusById ->
            questsByLocation.map { (location, quests) ->
                val updatedQuests = quests.map { quest ->

                    if (questsStatusById.contains(quest.id)) {
                        val status = questsStatusById.getValue(quest.id)
                        quest.copy(isCompleted = status.isCompleted)
                    } else {
                        quest
                    }
                }

                Pair(location, updatedQuests)
            }
        }
    }

    override suspend fun save(questStatus: QuestStatus) {
        val entity = QuestStatusEntity(
            uid = questStatus.id,
            isCompleted = questStatus.isCompleted,
            isHidden = questStatus.isHidden
        )

        return questStatusDao.insertAll(entity)
    }
}

private fun List<Quest>.groupContiguousItems(): List<Pair<String, List<Quest>>> =
    buildList {
        var previousLocation: String? = null
        var currentGroup = mutableListOf<Quest>()

        // iterate over each incoming value
        for (currentElement: Quest in this@groupContiguousItems) {
            if (previousLocation == null) {
                previousLocation = currentElement.location
            } else if (previousLocation != currentElement.location) {
                add(previousLocation to currentGroup.toList())
                previousLocation = currentElement.location
                currentGroup = mutableListOf()
            }

            currentGroup.add(currentElement)
        }
    }