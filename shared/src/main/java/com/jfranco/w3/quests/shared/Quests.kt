package com.jfranco.w3.quests.shared

import kotlinx.serialization.Serializable

interface QuestsRepository {
    fun all(): List<Quest>
}

@Serializable
sealed class Level {
    @Serializable
    data object Unaffected : Level()

    @Serializable
    data class Suggested(val level: Int) : Level()
}

@Serializable
sealed class Order {
    @Serializable
    data object Any : Order()

    @Serializable
    data class Suggested(val order: Int) : Order()
}

@Serializable
data class Quest(
    val location: String,
    val quest: String,
    val isCompleted: Boolean,
    val suggested: Level,
    val url: String,
    val branch: String?,
    val order: Order,
    val details: List<QuestDetail>,
)

@Serializable
data class QuestDetail(
    val detail: String,
    val isCompleted: Boolean,
)