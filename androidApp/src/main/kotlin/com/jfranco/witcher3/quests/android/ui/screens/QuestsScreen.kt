package com.jfranco.witcher3.quests.android.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jfranco.witcher3.quests.android.ui.components.QuestList
import com.jfranco.witcher3.quests.android.ui.components.QuestsTopBar

@Composable
fun QuestsScreen() {
    val viewModel: QuestsViewModel = questsViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Log.e("APP", "----> Updating ui - searchQuery=${state.searchQuery}")

    Scaffold(
        topBar = {
            QuestsTopBar(
                state,
//                viewModel::toggleSearch,
//                viewModel::updateSearchQuery,
//                viewModel::searchSelected,
//                viewModel::showCompletedQuests,
//                viewModel::hideCompletedQuests
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            QuestList(
                state,
                onCompletedChanged = { quest, isChecked ->
                    viewModel.save(quest, isChecked)
                }
            )
        }
    }
}