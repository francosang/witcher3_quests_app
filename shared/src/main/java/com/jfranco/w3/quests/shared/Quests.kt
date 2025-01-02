package com.jfranco.w3.quests.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface QuestsRepository {
    fun updates(): Flow<List<QuestsCollection>>
    suspend fun save(questStatus: QuestStatus)
    val quests: List<Quest>
}

data class QuestGroup(
    val name: String,
    val quests: List<Quest>
)

enum class QuestsCompleted {
    LOADING,
    SHOWING,
    HIDDEN
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

sealed class QuestsCollection {

    data class QuestsByLocation(val location: String, val quests: List<Quest>) :
        QuestsCollection()

    data class QuestsGrouped(val location: String, val questsGroups: List<QuestGroup>) :
        QuestsCollection()

    fun location(): String {
        return when (this) {
            is QuestsByLocation -> location
            is QuestsGrouped -> location
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
    val location: String,
    val quest: String,
    val isCompleted: Boolean,
    val suggested: Level,
    val url: String,
    val branch: String?,
    val order: Order,
    val extraDetails: List<ExtraDetail>,
    val type: Type = Type.Main
)

@Serializable
data class ExtraDetail(
    val detail: String,
    val link: String?,
    val isCompleted: Boolean,
)

@Serializable
data class QuestStatus(
    val id: Int,
    val isCompleted: Boolean,
    val isHidden: Boolean,
)