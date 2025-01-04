package com.jfranco.witcher3.quests.android.ui.screens

import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestsUiState(
    val hidingCompletedQuests: Boolean? = false,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Quest> = emptyList(),
    val scrollTo: Quest? = null,
    val questsCollection: List<QuestsCollection> = emptyList()
)

class QuestsViewModel(
    private val questsRepository: QuestsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuestsUiState())
    val state: StateFlow<QuestsUiState> = _state.stateIn(
        scope = viewModelScope,
        initialValue = QuestsUiState(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    init {
        viewModelScope.launch {
            val quests = questsRepository.getQuests()
            val lastCompletedQuest = questsRepository.getLastCompletedQuest()?.let { last ->
                quests.find { it.id == last.id }
            }

            val questsByLocation = quests.groupContiguousItemsByLocation()
            val questsCollections: List<QuestsCollection> =
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

            launch {
                questsRepository.questsStatusUpdates().collect { questsStatus ->
                    val questsStatusById = questsStatus.associateBy { it.id }
                    val updatedCollections = questsCollections
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

                    val hiding = state.value.hidingCompletedQuests

                    val visibleCollections =
                        updatedCollections.filter { if (hiding == true) !it.isCompleted else true }

                    _state.value = state.value.copy(
                        questsCollection = visibleCollections,
                        scrollTo = lastCompletedQuest
                    )
                }
            }

            launch {
                state.map { it.searchQuery }.distinctUntilChanged().collect { searchQuery ->
                    val searchResults =
                        quests.filter { it.quest.contains(searchQuery, ignoreCase = true) }

                    _state.value = state.value.copy(
                        searchResults = searchResults
                    )
                }
            }
        }
    }

    fun save(questStatus: QuestStatus) = viewModelScope.launch {
        questsRepository.save(questStatus)
    }

    fun showCompletedQuests() {
        _state.update { state ->
            state.copy(hidingCompletedQuests = false)
        }
    }

    fun hideCompletedQuests() {
        _state.update { state ->
            state.copy(hidingCompletedQuests = true)
        }
    }

    fun updateSearchQuery(text: String) {
        _state.update { state ->
            state.copy(searchQuery = text)
        }
    }

    fun toggleSearch(boolean: Boolean) {
        _state.update { state ->
            state.copy(
                isSearching = boolean,
                searchQuery = "",
            )
        }
    }

    fun searchSelected(quest: Quest) {
        _state.update { state ->
            state.copy(
                scrollTo = quest,
                isSearching = false,
                searchQuery = "",
            )
        }
    }

    fun onScrollEnd() {
        _state.update { state ->
            state.copy(
                scrollTo = null,
            )
        }
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