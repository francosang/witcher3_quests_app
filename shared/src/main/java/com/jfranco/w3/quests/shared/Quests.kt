package com.jfranco.w3.quests.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface QuestsRepository {
    fun questsStatusUpdates(): Flow<List<QuestStatus>>
    suspend fun save(questStatus: QuestStatus)
    suspend fun getQuests(): List<Quest>
    suspend fun getLastCompletedQuest(): QuestStatus?
}

sealed class QuestsCollection {

    data class QuestsByLocation(val location: String, val quests: List<Quest>) :
        QuestsCollection()

    data class QuestsGrouped(val location: String, val questsGroups: List<QuestGroup>) :
        QuestsCollection()

    val size by lazy {
        when (this) {
            is QuestsByLocation -> quests.size
            is QuestsGrouped -> questsGroups.sumOf { it.quests.size }
        }
    }
}

data class QuestGroup(
    val name: String,
    val quests: List<Quest>
)

@Serializable
sealed class QuestType(val show: String) {

    @Serializable
    data object Main : QuestType("Main")

    @Serializable
    data object Secondary : QuestType("Secondary")

    @Serializable
    data object Contract : QuestType("Contract")

    @Serializable
    data object TreasureHunt : QuestType("Treasure Hunt")

    @Serializable
    data object ScavengerHunt : QuestType("Scavenger Hunt")

    @Serializable
    data object GwentAndTheHeroesPursuits : QuestType("Gwent & The Heroes' Pursuits")

    @Serializable
    data object ChanceEncounters : QuestType("Chance Encounters")

}

@Serializable
sealed class Level {
    @Serializable
    data object Any : Level()

    @Serializable
    data class Suggested(val level: Int) : Level()

    fun show(): String {
        return when (this) {
            is Any -> "Any"
            is Suggested -> level.toString()
        }
    }
}

@Serializable
sealed class Order {
    @Serializable
    data object Any : Order()

    @Serializable
    data class Suggested(val order: Int) : Order()
}

@Serializable
sealed class Type(val type: String) {
    @Serializable
    data object Main : Type("Main")

    @Serializable
    data object Secondary : Type("Secondary")

    @Serializable
    data object Contracts : Type("Contract")

    @Serializable
    data object TreasureHunt : Type("Treasure Hunt")

}

@Serializable
data class Quest(
    val id: Int,
    val type: QuestType,
    val location: String,
    val quest: String,
    val isCompleted: Boolean,
    val suggested: Level,
    val url: String,
    val order: Order,
    val color: String,
    val extraDetails: List<ExtraDetail>,
    val considerIgnoring: Boolean,
    val branch: String?,
    val message: String?,
)

@Serializable
data class ExtraDetail(
    val detail: String,
    val link: String? = null,
    val isCompleted: Boolean = false,
)

@Serializable
data class QuestStatus(
    val id: Int,
    val isCompleted: Boolean,
    val isHidden: Boolean,
)


fun List<QuestsCollection>.filter(predicate: (Quest) -> Boolean): List<QuestsCollection> {
    return mapNotNull { collection ->
        when (collection) {
            is QuestsCollection.QuestsByLocation -> {
                collection.copy(
                    quests = collection.quests.filter(predicate)
                )
            }

            is QuestsCollection.QuestsGrouped -> {

                val groups = collection.questsGroups.mapNotNull {
                    val updatedQuests = it.quests.filter(predicate)

                    if (updatedQuests.isNotEmpty()) {
                        it.copy(quests = updatedQuests)
                    } else {
                        null
                    }
                }

                if (groups.isNotEmpty()) {
                    collection.copy(questsGroups = groups)
                } else {
                    null
                }
            }
        }
    }
}


fun List<Quest>.groupContiguousItemsByLocation(): List<Pair<String, List<Quest>>> {
    val list = this
    if (list.isEmpty()) return emptyList()
    return buildList {
        var previousLocation: String? = null
        var currentGroup = mutableListOf<Quest>()

        // iterate over each incoming value
        for (currentElement: Quest in list) {
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
}
