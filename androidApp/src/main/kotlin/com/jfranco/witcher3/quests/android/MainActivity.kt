package com.jfranco.witcher3.quests.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.jfranco.witcher3.quests.android.theme.AppTheme
import com.jfranco.witcher3.quests.android.ui.QuestsCompleted
import com.jfranco.witcher3.quests.android.ui.WitcherApp
import com.jfranco.witcher3.quests.android.ui.questsViewModel

@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(
                useDarkTheme = false
            ) {
                Surface {
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
                            WitcherApp()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsTopBar() {
    val viewModel = questsViewModel()
    val showing by viewModel.showingCompletedQuests.collectAsState()

    CenterAlignedTopAppBar(
        title = { Text("The Witcher 3 Quests") },
        actions = {
            IconButton(onClick = {

                if (showing == QuestsCompleted.SHOWING) {
                    viewModel.hideCompletedQuests()
                } else {
                    viewModel.showCompletedQuests()
                }

            }) {
                when (showing) {
                    QuestsCompleted.LOADING -> {}
                    QuestsCompleted.HIDDEN ->
                        Icon(painterResource(R.drawable.ic_shown), contentDescription = "Show")

                    QuestsCompleted.SHOWING ->
                        Icon(painterResource(R.drawable.ic_hidden), contentDescription = "Hide")

                }
            }


            IconButton(onClick = { /* Do something */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )

}

