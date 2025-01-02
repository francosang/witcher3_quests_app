package com.jfranco.witcher3.quests.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jfranco.witcher3.quests.android.ui.components.QuestsTopBar
import com.jfranco.witcher3.quests.android.ui.screens.QuestsScreen

@Composable
fun TheWitcher() {
    Scaffold(
        topBar = {
            QuestsTopBar()
        }
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            QuestsScreen()
        }
    }
}