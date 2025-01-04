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
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.witcher3.quests.android.ui.components.QuestList
import com.jfranco.witcher3.quests.android.ui.components.QuestsTopBar

@Composable
fun QuestsScreen() {
    val viewModel = questsViewModel()
    val state by questsViewModel().state.collectAsStateWithLifecycle()

    Log.i("APP", "Updating ui ${state.hashCode()}")

    Scaffold(
        topBar = {
            QuestsTopBar(state)
        }
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            QuestList(state, onScrollEnd = viewModel::onScrollEnd) { quest, isChecked ->
                viewModel.save(
                    QuestStatus(
                        id = quest.id,
                        isCompleted = isChecked,
                        isHidden = false
                    )
                )
            }
        }
    }
}