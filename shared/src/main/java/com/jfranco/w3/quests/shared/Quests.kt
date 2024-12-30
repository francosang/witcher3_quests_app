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