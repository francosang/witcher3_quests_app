package com.jfranco.witcher3.quests.android.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.w3.quests.shared.filter
import com.jfranco.witcher3.quests.android.MainApp
import com.jfranco.witcher3.quests.android.ui.components.IndexedLazyColumn
import com.jfranco.witcher3.quests.android.ui.components.IndexedLazyListState
import com.jfranco.witcher3.quests.android.ui.components.QuestCard
import com.jfranco.witcher3.quests.android.ui.components.rememberIndexedLazyListState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


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
        val indexedListState = rememberIndexedLazyListState()

        LaunchedEffect(Unit) {
            viewModel.searchSelectedQuest.collect {
                indexedListState.animateScrollToItem(it.id)
            }
        }

        QuestsList(
            questsByLocation,
            indexedListState,
            viewModel
        )
    }
}

@Composable
fun QuestsList(
    collection: List<QuestsCollection>,
    lazyListState: IndexedLazyListState,
    viewModel: QuestsViewModel
) {
    val expandedItems = remember { mutableStateListOf<Quest>() }

    fun onClick(quest: Quest) {
        if (expandedItems.contains(quest)) {
            expandedItems.remove(quest)
        } else {
            expandedItems.add(quest)
        }
    }

    IndexedLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        collection.forEach { collection ->
            when (collection) {
                is QuestsCollection.QuestsByLocation -> {
                    questGroupHeader(collection.location)
                    quests(
                        collection.quests,
                        viewModel,
                        expandedItems::contains,
                        ::onClick
                    )
                }

                is QuestsCollection.QuestsGrouped -> {
                    collection.questsGroups.forEach { (groupName, quests) ->
                        questGroupHeader(collection.location, groupName)
                        quests(
                            quests,
                            viewModel,
                            expandedItems::contains,
                            ::onClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.questGroupHeader(
    location: String,
    group: String? = null
) = stickyHeader {
    Surface(Modifier.fillParentMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                text = location,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .padding(top = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            if (group != null) {
                Text(
                    text = group,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


fun LazyListScope.quests(
    quests: List<Quest>,
    viewModel: QuestsViewModel,
    isSelected: (Quest) -> Boolean,
    onClick: (Quest) -> Unit,
) = items(quests, key = { it.id }) {
    QuestCard(
        modifier = Modifier.padding(horizontal = 12.dp),
        quest = it,
        isSelected = isSelected(it),
        onClick = onClick,
        onCompletedChanged = { isChecked ->
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


@Composable
fun questsViewModel(): QuestsViewModel {
    return viewModel<QuestsViewModel>(factory = QuestsViewModel.Factory)
}

class QuestsViewModel(
    private val questsRepository: QuestsRepository,
) : ViewModel() {

    private val _hidingCompletedQuests = MutableStateFlow<Boolean?>(false)
    val hidingCompletedQuests: StateFlow<Boolean?> = _hidingCompletedQuests

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchSelectedQuest = MutableSharedFlow<Quest>(replay = 0)
    val searchSelectedQuest: Flow<Quest> = _searchSelectedQuest

    val quests: List<Quest> = questsRepository.quests

    val filteredQuests = _searchQuery.map { query ->
        questsRepository.quests.filter {
            it.quest.contains(query, ignoreCase = true)
        }
    }

    fun updates(): Flow<List<QuestsCollection>> = combine(
        hidingCompletedQuests,
        questsRepository.updates(),
    ) { hiding, quests ->
        quests.filter {
            if (hiding == true) !it.isCompleted else true
        }
    }

    fun save(questStatus: QuestStatus) = viewModelScope.launch {
        questsRepository.save(questStatus)
    }

    fun showCompletedQuests() {
        _hidingCompletedQuests.value = false
    }

    fun hideCompletedQuests() {
        _hidingCompletedQuests.value = true
    }

    fun updateSearchQuery(text: String) {
        _searchQuery.value = text
    }

    fun toggleSearch(boolean: Boolean) {
        _isSearching.value = boolean
        if (boolean.not()) {
            updateSearchQuery("")
        }
    }

    fun searchSelected(quest: Quest) {
        viewModelScope.launch {
            _searchSelectedQuest.emit(quest)
            toggleSearch(false)
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
