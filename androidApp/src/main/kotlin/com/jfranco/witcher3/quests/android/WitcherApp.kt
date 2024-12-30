package com.jfranco.witcher3.quests.android

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.witcher3.quests.android.repo.JsonQuestsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WitcherApp() {
    var questsState by remember { mutableStateOf(listOf<Quest>()) }
    val groupedQuests = questsState.groupContiguousItems()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val inputStream = context.resources.openRawResource(R.raw.quests)
        questsState = getQuests(inputStream)
    }

    if (questsState.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Loading quests...")
            CircularProgressIndicator()
        }
    } else {
        val expandedItems = remember { mutableStateListOf<Quest>() }

        fun onClick(quest: Quest) {
            if (expandedItems.contains(quest)) {
                expandedItems.remove(quest)
            } else {
                expandedItems.add(quest)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            groupedQuests.forEach { pair ->
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = pair.first,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                                .padding(top = 8.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                items(pair.second) {
                    QuestListItem(
                        quest = it,
                        isSelected = expandedItems.contains(it),
                        onClick = ::onClick
                    )

//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                onClick(it)
//                            },
//                    ) {
//                        // Parent container
//                        Row(
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.fillMaxSize()
//                        ) {
//                            // Splits the item in two
//                            Column {
//                                // Base content, always visible
//                                Column(
//                                    modifier = Modifier
//                                        .padding(
//                                            top = 22.dp,
//                                            bottom = 10.dp
//                                        )
//                                        .padding(start = 16.dp)
//                                ) {
//                                    Row(Modifier.fillMaxWidth()) {
//                                        Column(Modifier.weight(0.5F)) {
//                                            Text(
//                                                text = it.quest,
//                                                color = Color.Black,
//                                                style = TextStyle(fontSize = 20.sp)
//                                            )
//                                            Spacer(
//                                                modifier = Modifier.padding(
//                                                    4.dp
//                                                )
//                                            )
//                                            Column {
//                                                Text(
//                                                    it.location.lowercase()
//                                                        .capitalize(),
//                                                    modifier = Modifier.padding(
//                                                        top = 8.dp
//                                                    ),
//                                                    style = TextStyle(
//                                                        fontSize = 14.sp
//                                                    )
//                                                )
//
//                                                val suggested = it.suggested
//
//                                                if (suggested is Level.Suggested) {
//                                                    Text(
//                                                        "Level: ${suggested.level}",
//                                                        modifier = Modifier.padding(
//                                                            top = 8.dp
//                                                        ),
//                                                        style = TextStyle(
//                                                            fontSize = 14.sp
//                                                        )
//                                                    )
//                                                }
//                                            }
//                                        }
//
//                                        Checkbox(
//                                            checked = it.isCompleted,
//                                            onCheckedChange = { isChecked ->
//                                                Log.i(
//                                                    "MainActivity",
//                                                    "Checked: $isChecked"
//                                                )
//                                            }
//                                        )
//                                    }
//                                }
//
//                                // The expandable content
//
//                                val details = it.extraDetails
//
//                                if (expandedItems.contains(it)) {
//                                    Column {
//                                        details.forEach { detail ->
//                                            HorizontalDivider()
//                                            Row(Modifier.fillMaxWidth()) {
//                                                Column(Modifier.weight(0.5F)) {
//                                                    Text(
//                                                        detail.detail,
//                                                        modifier = Modifier
//                                                            .clickable { }
//                                                            .padding(16.dp)
//                                                            .fillMaxWidth(),
//                                                        style = TextStyle(
//                                                            fontSize = 14.sp
//                                                        )
//                                                    )
//                                                }
//
//                                                Checkbox(
//                                                    checked = detail.isCompleted,
//                                                    onCheckedChange = { isChecked ->
//                                                        Log.i(
//                                                            "MainActivity",
//                                                            "Checked: $isChecked"
//                                                        )
//                                                    }
//                                                )
//                                            }
//                                        }
//                                    }
//                                }
//
//                                if (details.isNotEmpty()) {
//                                    Icon(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .clickable {
//                                                onClick(it)
//                                            },
//                                        imageVector = if (expandedItems.contains(
//                                                it
//                                            )
//                                        ) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                                        contentDescription = null
//                                    )
//                                }
//                            }
//                        }
//                    }

                }
            }
        }
    }
}


private suspend fun getQuests(inputStream: InputStream): List<Quest> {
    return withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.use { it.readText() }

        Log.i("MainActivity", content)

        JsonQuestsRepositoryImpl(content).all().apply {
            groupContiguousItems().forEach {
                Log.i("MainActivity", "Group: ${it.first}")
                it.second.forEach { quest ->
                    Log.i("MainActivity", "-> Quest: ${quest.quest}")
                }
            }
        }
    }
}

private fun List<Quest>.groupContiguousItems(): List<Pair<String, List<Quest>>> =
    buildList {
        var previousLocation: String? = null
        var currentGroup = mutableListOf<Quest>()

        // iterate over each incoming value
        for (currentElement: Quest in this@groupContiguousItems) {
            if (previousLocation == null) {
                previousLocation = currentElement.location
            } else if (previousLocation != currentElement.location) {
                add(previousLocation to currentGroup.toList())
                previousLocation = currentElement.location
                currentGroup = mutableListOf()
            }

            currentGroup.add(currentElement)
        }
    }