package com.jfranco.witcher3.quests.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.witcher3.quests.android.MainApp
import com.jfranco.witcher3.quests.android.common.state.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine

class QuestsViewModel(
    private val questsRepository: QuestsRepository,
) : BaseViewModel<QuestsState, Action>(QuestsState()) {

    override suspend fun initialization(): Flow<QuestsState> {
        return combine(
            questsRepository::getQuests.asFlow(),
            questsRepository::getLastCompletedQuest.asFlow(),
            questsRepository.questsStatusUpdates(),
        ) { quests, lastQuestUpdate, questsStatus ->
            // TODO: Avoid performing this logic every time
            val lastCompletedQuest = lastQuestUpdate?.let { last ->
                quests.find { it.id == last.id }
            }
            state {
                it.copy(
                    quests = quests,
                    scrollTo = Scroll.OnLoad(lastCompletedQuest),
                    questsStatusById = questsStatus.associateBy(QuestStatus::id)
                )
            }
        }
    }

    override suspend fun handle(state: QuestsState, action: Action): QuestsState {
        return when (action) {
            Action.HideCompletedQuests -> {
                state.copy(hidingCompletedQuests = true)
            }

            Action.ShowCompletedQuests -> {
                state.copy(hidingCompletedQuests = false)
            }

            is Action.ToggleSearch -> {
                state.copy(
                    isSearching = action.boolean,
                    searchQuery = if (action.boolean.not()) "" else state.searchQuery,
                )
            }

            is Action.UpdateSearchQuery -> {
                state.copy(searchQuery = action.text)
            }

            is Action.SearchSelected -> {
                state.copy(
                    scrollTo = Scroll.OnResult(action.quest),
                    isSearching = false,
                    searchQuery = "",
                )
            }

            is Action.UpdateQuestStatus -> {
                questsRepository.save(
                    QuestStatus(
                        action.quest.id,
                        action.completed,
                        false
                    )
                )

                state
            }
        }
    }

    fun save(quest: Quest, completed: Boolean) {
        emit(Action.UpdateQuestStatus(quest, completed))
    }

    fun showCompletedQuests() {
        emit(Action.ShowCompletedQuests)
    }

    fun hideCompletedQuests() {
        emit(Action.HideCompletedQuests)
    }

    fun updateSearchQuery(text: String) {
        emit(Action.UpdateSearchQuery(text))
    }

    fun toggleSearch(boolean: Boolean) {
        emit(Action.ToggleSearch(boolean))
    }

    fun resultSelected(quest: Quest) {
        emit(Action.SearchSelected(quest))
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