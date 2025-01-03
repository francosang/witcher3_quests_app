package com.jfranco.witcher3.quests.android.repo

import android.content.Context
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestGroup
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsCollection
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

    override val quests: List<Quest> by lazy {
        val inputStream = context.resources.openRawResource(R.raw.quests)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.use { it.readText() }
        Json.decodeFromString<List<Quest>>(content)
    }

    private val questsByLocation: List<QuestsCollection> by lazy {
        val questsByLocation = quests.groupContiguousItemsByLocation()

        questsByLocation.map { (location, allQuests) ->
            val (questsInOrder, questsAnyOrder) = allQuests.partition { it.order is Order.Suggested }

            val groups = buildList {
                if (questsInOrder.isNotEmpty())
                    add(QuestGroup("Complete in suggested order", questsInOrder))

                if (questsAnyOrder.isNotEmpty())
                    add(QuestGroup("Complete in any order at any time", questsAnyOrder))
            }

            QuestsCollection.QuestsGrouped(
                location,
                groups
            )
        }
    }


    override fun updates(): Flow<List<QuestsCollection>> {
        return questStatusDao.changes()
            .map { list -> list.associateBy { it.uid } }
            .map { questsStatusById ->
                questsByLocation
                    .filterIsInstance<QuestsCollection.QuestsGrouped>()
                    .map { locationAndQuests ->
                        val updatedGroups = locationAndQuests.questsGroups.map { questGroup ->
                            questGroup.copy(
                                quests = questGroup.quests.map { quest ->
                                    if (questsStatusById.contains(quest.id)) {
                                        val status = questsStatusById.getValue(quest.id)
                                        quest.copy(isCompleted = status.isCompleted)
                                    } else {
                                        quest
                                    }
                                }
                            )
                        }
                        locationAndQuests.copy(questsGroups = updatedGroups)
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

private fun List<Quest>.groupContiguousItemsByLocation(): List<Pair<String, List<Quest>>> =
    buildList {
        var previousLocation: String? = null
        var currentGroup = mutableListOf<Quest>()

        // iterate over each incoming value
        for (currentElement: Quest in this@groupContiguousItemsByLocation) {
            if (previousLocation == null) {
                previousLocation = currentElement.location
            } else if (previousLocation != currentElement.location) {
                add(previousLocation to currentGroup.toList())
                previousLocation = currentElement.location
                currentGroup = mutableListOf()
            }

            currentGroup.add(currentElement)
        }

        add(previousLocation!! to currentGroup.toList())
    }
