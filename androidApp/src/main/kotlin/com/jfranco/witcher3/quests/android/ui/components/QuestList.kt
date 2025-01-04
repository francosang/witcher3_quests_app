package com.jfranco.witcher3.quests.android.ui.components

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestsCollection
import com.jfranco.witcher3.quests.android.ui.screens.QuestsUiState

@Composable
fun QuestList(
    state: QuestsUiState,
    onCompletedChanged: (Quest, Boolean) -> Unit
) {
    val questsByLocation = state.questsCollection

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

        if (state.searchResultSelected != null) {
            LaunchedEffect(state.searchResultSelected) {
                indexedListState.animateScrollToItem(state.searchResultSelected.id)
            }
        }

        QuestsList(
            questsByLocation,
            indexedListState,
            onCompletedChanged
        )
    }
}

@Composable
fun QuestsList(
    collection: List<QuestsCollection>,
    lazyListState: IndexedLazyListState,
    onCompletedChanged: (Quest, Boolean) -> Unit
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
                        expandedItems::contains,
                        ::onClick,
                        onCompletedChanged
                    )
                }

                is QuestsCollection.QuestsGrouped -> {
                    collection.questsGroups.forEach { (groupName, quests) ->
                        questGroupHeader(collection.location, groupName)
                        quests(
                            quests,
                            expandedItems::contains,
                            ::onClick,
                            onCompletedChanged
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
    isSelected: (Quest) -> Boolean,
    onClick: (Quest) -> Unit,
    onCompletedChanged: (Quest, Boolean) -> Unit
) = items(quests, key = { it.id }) {
    QuestCard(
        modifier = Modifier.padding(horizontal = 12.dp),
        quest = it,
        isSelected = isSelected(it),
        onClick = onClick,
        onCompletedChanged = { isChecked ->
            onCompletedChanged(it, isChecked)
        }
    )
}

