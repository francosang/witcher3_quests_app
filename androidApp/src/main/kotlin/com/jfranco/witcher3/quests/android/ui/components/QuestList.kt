package com.jfranco.witcher3.quests.android.ui.components

import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestsCollection
import com.jfranco.witcher3.quests.android.common.ui.IndexedLazyColumn
import com.jfranco.witcher3.quests.android.common.ui.IndexedLazyListState
import com.jfranco.witcher3.quests.android.common.ui.rememberIndexedLazyListState
import com.jfranco.witcher3.quests.android.ui.screens.QuestsState
import com.jfranco.witcher3.quests.android.ui.screens.Scroll

@Composable
fun QuestList(
    state: QuestsState,
    onCompletedChanged: (Quest, Boolean) -> Unit
) {
    val questsByLocation = state.collections

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

        // to avoid repeating the animation on config changes, etc
        var initialScroll by rememberSaveable { mutableStateOf(false) }
        var lastResultSelected by rememberSaveable { mutableStateOf<Int?>(null) }

        val scrollQuest = state.scrollTo?.let {
            remember(it) {
                when (it) {
                    is Scroll.OnLoad ->
                        if (initialScroll.not()) {
                            initialScroll = true
                            it.quest
                        } else {
                            null
                        }

                    is Scroll.OnResult -> if (lastResultSelected != it.quest.id) {
                        lastResultSelected = it.quest.id
                        it.quest
                    } else {
                        null
                    }
                }
            }
        }

        LaunchedEffect(state.scrollTo) {
            if (scrollQuest != null) {
                indexedListState.animateScrollToItem(scrollQuest.id)
            }
        }

        QuestsList(
            questsByLocation,
            scrollQuest,
            indexedListState,
            onCompletedChanged
        )
    }
}

@Composable
fun QuestsList(
    collection: List<QuestsCollection>,
    highlighted: Quest? = null,
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
                        highlighted,
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
                            highlighted,
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
    highlighted: Quest? = null,
    onClick: (Quest) -> Unit,
    onCompletedChanged: (Quest, Boolean) -> Unit
) = items(quests, key = { it.id }) {
    QuestCard(
        modifier = Modifier.padding(horizontal = 12.dp),
        quest = it,
        isHighlighted = highlighted?.id == it.id,
        isSelected = isSelected(it),
        onClick = onClick,
        onCompletedChanged = { isChecked ->
            onCompletedChanged(it, isChecked)
        }
    )
}

