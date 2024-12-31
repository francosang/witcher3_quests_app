package com.jfranco.w3.quests.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface QuestsRepository {
    fun updates(): Flow<List<Pair<String, List<Quest>>>>
    suspend fun save(questStatus: QuestStatus)
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