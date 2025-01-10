package com.jfranco.witcher3.quests.android.ui.screens

import com.jfranco.w3.quests.shared.Quest

sealed class Action {
    data object ShowCompletedQuests : Action()
    data object HideCompletedQuests : Action()
    data class UpdateSearchQuery(val text: String) : Action()
    data class ToggleSearch(val boolean: Boolean) : Action()
    data class SearchSelected(val quest: Quest) : Action()
    data class UpdateQuestStatus(val quest: Quest, val completed: Boolean) : Action()
}
