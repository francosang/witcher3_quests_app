package com.jfranco.witcher3.quests.android.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestGroup
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsCollection
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.w3.quests.shared.filter
import com.jfranco.w3.quests.shared.groupContiguousItemsByLocation
import com.jfranco.witcher3.quests.android.MainApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class QuestsUiState(
    private val quests: List<Quest> = emptyList(),
    private val questsStatusById: Map<Int, QuestStatus> = emptyMap(),
    val hidingCompletedQuests: Boolean = false,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val highlightQuest: Quest? = null,
    val scrollTo: Quest? = null,
) {

    val questsCollections: List<QuestsCollection> = quests
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

}

sealed class Actions {
    data object ShowCompletedQuests : Actions()
    data object HideCompletedQuests : Actions()
    data class UpdateSearchQuery(val text: String) : Actions()
    data class ToggleSearch(val boolean: Boolean) : Actions()
    data class SearchSelected(val quest: Quest) : Actions()
    data class UpdateQuestStatus(val quest: Quest, val completed: Boolean) : Actions()
}


open class QuestsViewModel(
    private val questsRepository: QuestsRepository,
) : ViewModel() {

    private val actions = MutableSharedFlow<Actions>(replay = 1)

    private val initialState = QuestsUiState()
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<QuestsUiState>
        get() = _state.stateIn(
            scope = viewModelScope,
            initialValue = initialState,
            started = SharingStarted.WhileSubscribed(5000)
        )

    init {
        viewModelScope.launch {
            Log.w("APP", "Initial load")

            val quests = questsRepository.getQuests()
            val lastCompletedQuest = questsRepository.getLastCompletedQuest()?.let { last ->
                quests.find { it.id == last.id }
            }

            _state.update {
                it.copy(
                    quests = quests,
                    scrollTo = lastCompletedQuest,
                )
            }
        }
        viewModelScope.launch {
            questsRepository.questsStatusUpdates().collect { questsStatus ->
                _state.update {
                    it.copy(
                        questsStatusById = questsStatus.associateBy(QuestStatus::id)
                    )
                }
            }

            Log.e("APP", "----> Done collecting status updates")
        }

        viewModelScope.launch {
            actions.map {
                val prev = _state.value
                val newState = handle(prev, it)

                Log.w("APP", "Prev state - ${prev.hashCode()}")
                Log.w("APP", "Action     - ${it.hashCode()}")
                Log.w("APP", "New  state - ${newState.hashCode()}")

                newState
            }.collect {
                _state.value = it
            }

            Log.e("APP", "----> Done collecting actions")
        }
    }

    protected open suspend fun handle(previous: QuestsUiState, action: Actions): QuestsUiState {
        return when (action) {
            Actions.HideCompletedQuests -> {
                previous.copy(hidingCompletedQuests = true)
            }

            Actions.ShowCompletedQuests -> {
                previous.copy(hidingCompletedQuests = false)
            }

            is Actions.ToggleSearch -> {
                previous.copy(
                    isSearching = action.boolean,
                    searchQuery = if (action.boolean.not()) "" else previous.searchQuery,
                )
            }

            is Actions.UpdateSearchQuery -> {
                previous.copy(searchQuery = action.text)
            }

            is Actions.SearchSelected -> {
                previous.copy(
                    scrollTo = action.quest,
                    isSearching = false,
                    searchQuery = "",
                )
            }

            is Actions.UpdateQuestStatus -> {
                questsRepository.save(
                    QuestStatus(
                        action.quest.id,
                        action.completed,
                        false
                    )
                )

                previous
            }
        }
    }

    fun save(quest: Quest, completed: Boolean) {
        actions.tryEmit(Actions.UpdateQuestStatus(quest, completed))
    }

    fun showCompletedQuests() {
        actions.tryEmit(Actions.ShowCompletedQuests)
    }

    fun hideCompletedQuests() {
        actions.tryEmit(Actions.HideCompletedQuests)
    }

    fun updateSearchQuery(text: String) {
        actions.tryEmit(Actions.UpdateSearchQuery(text))
    }

    fun toggleSearch(boolean: Boolean) {
        actions.tryEmit(Actions.ToggleSearch(boolean))
    }

    fun searchSelected(quest: Quest) {
        actions.tryEmit(Actions.SearchSelected(quest))
    }

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY]) as MainApp

                @Suppress("UNCHECKED_CAST")
                return QuestsViewModel(
                    application.container.questsRepository,
                ) as T
            }
        }
    }
}

@Composable
fun questsViewModel(): QuestsViewModel {
    return viewModel<QuestsViewModel>(factory = QuestsViewModel.Factory)
}