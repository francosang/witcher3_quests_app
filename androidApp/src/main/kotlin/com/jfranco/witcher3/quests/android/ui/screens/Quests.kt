package com.jfranco.witcher3.quests.android.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsCollection
import com.jfranco.w3.quests.shared.QuestsCompleted
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.witcher3.quests.android.MainApp
import com.jfranco.witcher3.quests.android.ui.components.QuestCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestsScreen() {
    val viewModel = questsViewModel()
    val questsByLocation by viewModel.updates().collectAsStateWithLifecycle(emptyList())

    if (questsByLocation.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Loading quests...")
            CircularProgressIndicator()
        }
    } else {
        val expandedItems = remember { mutableStateListOf<Quest>() }

        fun onClick(quest: Quest) {
            if (expandedItems.contains(quest)) {
                expandedItems.remove(quest)
            } else {
                expandedItems.add(quest)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            questsByLocation
                .forEach { collection ->
                    when (collection) {
                        is QuestsCollection.QuestsByLocation -> {
                            stickyHeader {
                                Surface(Modifier.fillParentMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
                                            .wrapContentHeight()
                                    ) {
                                        Text(
                                            text = collection.location(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp)
                                                .padding(top = 8.dp),
                                            style = MaterialTheme.typography.headlineSmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            items(collection.quests) {
                                QuestCard(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    quest = it,
                                    isSelected = expandedItems.contains(it),
                                    onClick = ::onClick,
                                    onCompletedChanged = { isChecked ->
                                        Log.i(
                                            "MainActivity",
                                            "Checked: $isChecked"
                                        )

                                        viewModel.save(
                                            QuestStatus(
                                                id = it.id,
                                                isCompleted = isChecked,
                                                isHidden = false
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        is QuestsCollection.QuestsGrouped -> {

                            collection.questsGroups.forEach { (groupName, quests) ->
                                stickyHeader {
                                    Surface(Modifier.fillParentMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                        ) {
                                            Text(
                                                text = collection.location(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(6.dp)
                                                    .padding(top = 8.dp),
                                                style = MaterialTheme.typography.headlineSmall,
                                                textAlign = TextAlign.Center
                                            )

                                            Text(
                                                text = groupName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                items(quests) {
                                    QuestCard(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        quest = it,
                                        isSelected = expandedItems.contains(it),
                                        onClick = ::onClick,
                                        onCompletedChanged = { isChecked ->
                                            Log.i(
                                                "MainActivity",
                                                "Checked: $isChecked"
                                            )

                                            viewModel.save(
                                                QuestStatus(
                                                    id = it.id,
                                                    isCompleted = isChecked,
                                                    isHidden = false
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}


@Composable
fun questsViewModel(): QuestsViewModel {
    return viewModel<QuestsViewModel>(factory = QuestsViewModel.Factory)
}

class QuestsViewModel(
    private val questsRepository: QuestsRepository,
) : ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _showingCompletedQuests = MutableStateFlow(QuestsCompleted.SHOWING)

    val showingCompletedQuests: StateFlow<QuestsCompleted> = _showingCompletedQuests

    fun count(): Int {
        return questsRepository.quests.count()
    }

    fun updates(): Flow<List<QuestsCollection>> {
        return combine(showingCompletedQuests, questsRepository.updates()) { showing, quests ->
            if (showing == QuestsCompleted.SHOWING) {
                quests
            } else {
                quests.mapNotNull { collection ->
                    when (collection) {
                        is QuestsCollection.QuestsByLocation -> {
                            collection.copy(
                                quests = collection.quests.filter { !it.isCompleted }
                            )
                        }

                        is QuestsCollection.QuestsGrouped -> {

                            val groups = collection.questsGroups.mapNotNull {
                                val updatedQuests = it.quests.filter { q -> !q.isCompleted }

                                if (updatedQuests.isNotEmpty()) {
                                    it.copy(quests = updatedQuests)
                                } else {
                                    null
                                }
                            }

                            if (groups.isNotEmpty()) {
                                collection.copy(questsGroups = groups)
                            } else {
                                null
                            }
                        }
                    }
                }
            }
        }
    }

    fun save(questStatus: QuestStatus) = viewModelScope.launch {
        questsRepository.save(questStatus)
    }

    fun showCompletedQuests() {
        _showingCompletedQuests.value = QuestsCompleted.SHOWING
    }

    fun hideCompletedQuests() {
        _showingCompletedQuests.value = QuestsCompleted.HIDDEN
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
