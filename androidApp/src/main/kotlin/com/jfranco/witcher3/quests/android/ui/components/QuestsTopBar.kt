package com.jfranco.witcher3.quests.android.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import com.jfranco.w3.quests.shared.QuestsCompleted
import com.jfranco.witcher3.quests.android.R
import com.jfranco.witcher3.quests.android.ui.screens.questsViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsTopBar() {
    val viewModel = questsViewModel()
    val showing by viewModel.showingCompletedQuests.collectAsState()

    var searching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    CenterAlignedTopAppBar(
        title = {
            if (searching) {
                var searchQuery by remember { mutableStateOf("") }
                var active by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                PredictiveBackHandler(enabled = true) { progress: Flow<BackEventCompat> ->
                    try {
                        progress.collect {}
                        active = false
                        searching = false
                        searchQuery = ""
                    } catch (_: CancellationException) {
                    }
                }

                SearchBarDefaults.InputField(
                    modifier = Modifier.focusRequester(focusRequester),
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        active = false
                        searching = false
                        searchQuery = ""
                    },
                    expanded = active,
                    onExpandedChange = { active = it },
                    placeholder = { Text("Search") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingIcon = {
                        if (active)
                            Icon(
                                modifier = Modifier.clickable {
                                    searchQuery = ""
                                    searching = false
                                    active = false
                                },
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null
                            )
                    },
                )
            } else {
                Text("The Witcher 3 Quests")
            }
        },
        actions = {
            if (searching.not()) {
                IconButton(onClick = {
                    searching = true
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

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
        }
    )

}