package com.jfranco.witcher3.quests.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jfranco.witcher3.quests.android.R
import com.jfranco.witcher3.quests.android.ui.screens.questsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsTopBar() {
    val viewModel = questsViewModel()
    val hiding by viewModel.hidingCompletedQuests.collectAsState()
    val quests by viewModel.filteredQuests.collectAsState(viewModel.quests)
    val searchText by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val focusRequester = remember { FocusRequester() }

    Row {
        SearchBar(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier.focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            "The Witcher 3 Quests",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    trailingIcon = {
                        Row {
                            if (isSearching) {
                                IconButton(onClick = {
                                    viewModel.toggleSearch(false)
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close search"
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    focusRequester.requestFocus()
                                    viewModel.toggleSearch(true)
                                }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Open search"
                                    )
                                }
                            }

                            IconButton(onClick = {
                                when (hiding) {
                                    true -> viewModel.showCompletedQuests()
                                    false -> viewModel.hideCompletedQuests()
                                    null -> {}
                                }
                            }) {

                                when (hiding) {
                                    true -> Icon(
                                        painterResource(R.drawable.ic_shown),
                                        contentDescription = "Show completed quests"
                                    )

                                    false -> Icon(
                                        painterResource(R.drawable.ic_hidden),
                                        contentDescription = "Hide"
                                    )

                                    null -> {}
                                }
                            }
                        }
                    },
                    query = searchText,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = viewModel::updateSearchQuery,
                    expanded = isSearching,
                    onExpandedChange = viewModel::toggleSearch,
                )
            },
            expanded = isSearching,
            onExpandedChange = viewModel::toggleSearch,
        ) {
            LazyColumn {
                items(quests) { quest ->
                    Text(
                        text = quest.quest,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                viewModel.searchSelected(quest)
                            }
                    )
                }
            }
        }
    }

//    TopAppBar(
//        title = {

//            if (searching) {
//                val searchQuery by viewModel.querySearchQuery.collectAsState()
//                var active by remember { mutableStateOf(false) }
//
//                LaunchedEffect(Unit) {
//                    focusRequester.requestFocus()
//                }
//
//                fun onClose() {
//                    searching = false
//                    active = false
//                    viewModel.updateSearchQuery("")
//                }
//
//                PredictiveBackHandler(enabled = true) { progress: Flow<BackEventCompat> ->
//                    try {
//                        progress.collect {}
//                        onClose()
//                    } catch (_: CancellationException) {
//                    }
//                }
//
//                SearchBarDefaults.InputField(
//                    modifier = Modifier.focusRequester(focusRequester),
//                    query = searchQuery,
//                    onQueryChange = {
//                        viewModel.updateSearchQuery(it)
//                    },
//                    onSearch = {
//                        onClose()
//                    },
//                    expanded = active,
//                    onExpandedChange = { active = it },
//                    placeholder = { Text("Search") },
//                    leadingIcon = {
//                        Icon(
//                            imageVector = Icons.Rounded.Search,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                        )
//                    },
//                    trailingIcon = {
//                        if (active)
//                            Icon(
//                                modifier = Modifier.clickable {
//                                    onClose()
//                                },
//                                imageVector = Icons.Rounded.Close,
//                                contentDescription = null
//                            )
//                    },
//                )
//            } else {
//                Text("The Witcher 3 Quests")
//            }
//        },
//        actions = {
////            if (searching.not()) {
////                IconButton(onClick = {
////                    searching = true
////                }) {
////                    Icon(Icons.Default.Search, contentDescription = "Search")
////                }
////            }
//
//            IconButton(onClick = {
//                if (showing == QuestsCompleted.SHOWING) {
//                    viewModel.hideCompletedQuests()
//                } else {
//                    viewModel.showCompletedQuests()
//                }
//            }) {
//                when (showing) {
//                    QuestsCompleted.LOADING -> {}
//                    QuestsCompleted.HIDDEN ->
//                        Icon(painterResource(R.drawable.ic_shown), contentDescription = "Show")
//
//                    QuestsCompleted.SHOWING ->
//                        Icon(painterResource(R.drawable.ic_hidden), contentDescription = "Hide")
//
//                }
//            }
//        }
//    )

}