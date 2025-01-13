package com.jfranco.witcher3.quests.android.ui.screens

import androidx.compose.runtime.Immutable
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestGroup
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsCollection
import com.jfranco.w3.quests.shared.filter
import com.jfranco.w3.quests.shared.groupContiguousItemsByLocation

sealed class Scroll {
    data class OnLoad(val quest: Quest?) : Scroll()
    data class OnResult(val quest: Quest) : Scroll()
}

@Immutable
data class QuestsState(
    private val quests: List<Quest> = emptyList(),
    private val questsStatusById: Map<Int, QuestStatus> = emptyMap(),
    val hidingCompletedQuests: Boolean = false,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val highlightQuest: Quest? = null,
    val scrollTo: Scroll? = null,
) {

    val collections: List<QuestsCollection> = quests
        .groupContiguousItemsByLocation()
        .map { (location, allQuests) ->
            val (questsInOrder, questsAnyOrder) = allQuests.partition { it.order is Order.Suggested }

            val groups = buildList {
                if (questsInOrder.isNotEmpty())
                    this.add(QuestGroup("Complete in suggested order", questsInOrder))

                if (questsAnyOrder.isNotEmpty())
                    this.add(QuestGroup("Complete in any order at any time", questsAnyOrder))
            }

            QuestsCollection.QuestsGrouped(
                location,
                groups
            )
        }
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
        }.filter { hidingCompletedQuests && !it.isCompleted || !hidingCompletedQuests }


    val searchResults = quests
        .filter { it.quest.contains(searchQuery, ignoreCase = true) }

    override fun toString(): String = """QuestsUiState(
                quests=${quests.size}, 
                collections=${collections.size}, 
                searchResults=${searchResults.size}, 
                hidingCompletedQuests=$hidingCompletedQuests, 
                isSearching=$isSearching, 
                searchQuery='$searchQuery', 
                highlightQuest=$highlightQuest, 
                scrollTo=$scrollTo,
            )""".trimIndent()
}