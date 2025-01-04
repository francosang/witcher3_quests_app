package com.jfranco.witcher3.quests.android.ui.screens

import android.util.Log
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestsUiState(
    val loading: Boolean = true,
    val hidingCompletedQuests: Boolean? = false,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Quest> = emptyList(),
    val searchResultSelected: Quest? = null,
    val quests: List<Quest> = emptyList(),
    val questsCollection: List<QuestsCollection> = emptyList()
)

@Suppress("OPT_IN_USAGE")
class QuestsViewModel(
    private val questsRepository: QuestsRepository,
) : ViewModel() {

    // the initial load, emits one single element
    private val allQuestsInitialLoad: Flow<List<Quest>> = flow {
        Log.e("APP", "LOADING quests!")
        val quests = questsRepository.getQuests()
        emit(quests)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    // groups all the quests in collections, by their location and order
    private val questCollections: Flow<List<QuestsCollection>> =
        allQuestsInitialLoad.map { quests ->
            Log.e("APP", "GROUPING quests!")
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

    // updates all the quests as their status change
    private val questCollectionUpdates: Flow<List<QuestsCollection>> = combine(
        questsRepository.questsStatusUpdates(),
        questCollections,
    ) { questsStatus, collections ->
        val questsStatusById = questsStatus.associateBy { it.id }
        collections
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

    private val _state = MutableStateFlow(QuestsUiState(loading = true))

    private val questsVisible = questCollectionUpdates.flatMapLatest { collections ->
        _state.map { it.hidingCompletedQuests }.distinctUntilChanged().map { hiding ->
            collections.filter { if (hiding == true) !it.isCompleted else true }
        }
    }

    private val searchResults = allQuestsInitialLoad.flatMapLatest { quests ->
        _state.map { it.searchQuery }.distinctUntilChanged().map { searchQuery ->
            quests.filter { it.quest.contains(searchQuery, ignoreCase = true) }
        }
    }

    val state: StateFlow<QuestsUiState> = combine(
        _state,
        questsVisible,
        searchResults
    ) { state, visibleQuests, searchResults ->
        state.copy(
            questsCollection = visibleQuests,
            searchResults = searchResults
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = QuestsUiState(loading = true),
        started = SharingStarted.WhileSubscribed(5000)
    )

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
                searchResultSelected = quest,
                isSearching = false,
                searchQuery = "",
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