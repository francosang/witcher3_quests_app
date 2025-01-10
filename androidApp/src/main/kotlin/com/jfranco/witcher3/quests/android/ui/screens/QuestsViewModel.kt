package com.jfranco.witcher3.quests.android.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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

    private val quests: Flow<List<Quest>> = flow {
        emit(questsRepository.getQuests())
    }

    private val lastCompletedQuest: Flow<QuestStatus?> = flow {
        emit(questsRepository.getLastCompletedQuest())
    }

    private val questsUpdates = questsRepository.questsStatusUpdates()

    private val ref = atomic(QuestsUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val state = flow {
        val initialization = combine(
            quests,
            lastCompletedQuest,
            questsUpdates,
        ) { quests, lastQuestUpdate, questsStatus ->
            // TODO: Avoid performing this logic every time
            val lastCompletedQuest = lastQuestUpdate?.let { last ->
                quests.find { it.id == last.id }
            }

            ref.updateAndGet {
                it.copy(
                    quests = quests,
                    scrollTo = lastCompletedQuest,
                    questsStatusById = questsStatus.associateBy(QuestStatus::id)
                )
            }
        }

        val actions = actions.map {
            Log.i("APP", "Action: $it")

            val prev = ref.value
            val newState = handle(prev, it)
            ref.updateAndGet { newState }
        }

        emitAll(listOf(initialization, actions).asFlow().flattenMerge())
    }.distinctUntilChanged()

    @Composable
    fun collectStateWithLifecycle() = state.collectAsStateWithLifecycle(ref.value)

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

    fun resultSelected(quest: Quest) {
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