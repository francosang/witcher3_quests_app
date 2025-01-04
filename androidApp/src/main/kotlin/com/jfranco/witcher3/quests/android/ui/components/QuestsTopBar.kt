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
import com.jfranco.witcher3.quests.android.ui.screens.QuestsUiState
import com.jfranco.witcher3.quests.android.ui.screens.questsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsTopBar(state: QuestsUiState) {
    val viewModel = questsViewModel()
    val hidingCompletedQuests = state.hidingCompletedQuests
    val searchResults = state.searchResults
    val searchQuery = state.searchQuery
    val isSearching = state.isSearching

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
                                when (hidingCompletedQuests) {
                                    true -> viewModel.showCompletedQuests()
                                    false -> viewModel.hideCompletedQuests()
                                    null -> {}
                                }
                            }) {

                                when (hidingCompletedQuests) {
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
                    query = searchQuery,
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
                items(searchResults) { quest ->
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
}