package com.jfranco.witcher3.quests.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.witcher3.quests.android.repo.JsonQuestsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                var quests by remember { mutableStateOf(listOf<Quest>()) }

                LaunchedEffect(Unit) {
                    quests = getQuests(R.raw.quests)
                }

                Scaffold(
                    topBar = {
                        Surface(shadowElevation = 5.dp) {
                            CenterAlignedTopAppBar(title = { Text("The Witcher 3 Quests") })
                        }
                    }
                ) { paddingValues ->
                    Box(
                        Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        if (quests.isEmpty()) {
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

                            LazyColumn(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(quests) {
                                    fun onClick(quest: Quest) {
                                        if (expandedItems.contains(quest)) {
                                            expandedItems.remove(quest)
                                        } else {
                                            expandedItems.add(quest)
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onClick(it)
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.elevatedCardElevation(
                                            defaultElevation = 5.dp
                                        )
                                    ) {
                                        // Parent container
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            // Splits the item in two
                                            Column {
                                                // Base content, always visible
                                                Column(
                                                    modifier = Modifier
                                                        .padding(top = 22.dp, bottom = 10.dp)
                                                        .padding(start = 16.dp)
                                                ) {
                                                    Row(Modifier.fillMaxWidth()) {
                                                        Column(Modifier.weight(0.5F)) {
                                                            Text(
                                                                text = it.quest,
                                                                color = Color.Black,
                                                                style = TextStyle(fontSize = 20.sp)
                                                            )
                                                            Spacer(modifier = Modifier.padding(4.dp))
                                                            Column {
                                                                Text(
                                                                    it.location.lowercase()
                                                                        .capitalize(),
                                                                    modifier = Modifier.padding(top = 8.dp),
                                                                    style = TextStyle(fontSize = 14.sp)
                                                                )

                                                                val suggested = it.suggested

                                                                if (suggested is Level.Suggested) {
                                                                    Text(
                                                                        "Level: ${suggested.level}",
                                                                        modifier = Modifier.padding(
                                                                            top = 8.dp
                                                                        ),
                                                                        style = TextStyle(fontSize = 14.sp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Checkbox(
                                                            checked = it.isCompleted,
                                                            onCheckedChange = { isChecked ->
                                                                Log.i(
                                                                    "MainActivity",
                                                                    "Checked: $isChecked"
                                                                )
                                                            }
                                                        )
                                                    }
                                                }

                                                // The expandable content

                                                val details = it.extraDetails

                                                if (expandedItems.contains(it)) {
                                                    Column {
                                                        details.forEach { detail ->
                                                            HorizontalDivider()
                                                            Row(Modifier.fillMaxWidth()) {
                                                                Column(Modifier.weight(0.5F)) {
                                                                    Text(
                                                                        detail.detail,
                                                                        modifier = Modifier
                                                                            .clickable { }
                                                                            .padding(16.dp)
                                                                            .fillMaxWidth(),
                                                                        style = TextStyle(fontSize = 14.sp)
                                                                    )
                                                                }

                                                                Checkbox(
                                                                    checked = detail.isCompleted,
                                                                    onCheckedChange = { isChecked ->
                                                                        Log.i(
                                                                            "MainActivity",
                                                                            "Checked: $isChecked"
                                                                        )
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                if (details.isNotEmpty()) {
                                                    Icon(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                onClick(it)
                                                            },
                                                        imageVector = if (expandedItems.contains(
                                                                it
                                                            )
                                                        ) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                    }

                                }
                            }

//                            LazyColumn(
//                                modifier = Modifier
//                                    .padding(horizontal = 16.dp),
//                                verticalArrangement = Arrangement.spacedBy(20.dp)
//                            ) {
//                                items(quests) { item ->
//                                    AnimatedVisibility(
//                                        visible = true,
//                                        enter = expandVertically(),
//                                        exit = shrinkVertically()
//                                    ) {
//                                        Box(
//                                            Modifier.animateEnterExit(
//                                                enter = scaleIn(),
//                                                exit = scaleOut()
//                                            )
//                                        ) {
//                                            QuestItem(
//                                                item,
//                                                onClick = {
//                                                    Log.i(
//                                                        "MainActivity",
//                                                        "Clicked on ${item.quest}"
//                                                    )
//                                                },
//                                            )
//                                        }
//                                    }
//                                }
//                            }


//                            QuestGroups(
//                                items = quests,
//                                showChildren = expandedItems::contains,
//                                group = {
//                                    Card(
//                                        Modifier
//                                            .fillMaxWidth()
//                                            .clickable {
//                                                if (expandedItems.contains(it)) {
//                                                    expandedItems.remove(it)
//                                                } else {
//                                                    expandedItems.add(it)
//                                                }
//                                            }
//                                    ) {
//                                        Text(
//                                            it.quest,
//                                        )
//                                    }
//
//                                },
//                                children = {
//                                    items(it.details) { pr ->
//                                        Text(
//                                            pr.detail,
//                                            Modifier
//                                                .padding(start = 6.dp)
//                                                .fillMaxWidth()
//                                        )
//                                    }
//                                }
//                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun getQuests(resourceId: Int): List<Quest> {
        return withContext(Dispatchers.IO) {
            val inputStream = resources.openRawResource(resourceId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }

            Log.i("MainActivity", content)

            JsonQuestsRepositoryImpl(content).all()
        }
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

//
//@Composable
//fun QuestItem(
//    item: Quest,
//    onClick: () -> Unit,
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable {
//                onClick()
//            },
//        shape = RoundedCornerShape(8.dp),
//        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = item.quest,
//                    color = Color.Black,
//                    style = TextStyle(fontSize = 20.sp)
//                )
//                Spacer(modifier = Modifier.padding(4.dp))
//                Column {
//                    Text(
//                        item.location.lowercase().capitalize(),
//                        modifier = Modifier.padding(top = 8.dp),
//                        style = TextStyle(fontSize = 14.sp)
//                    )
//
//                    val suggested = it.suggested
//
//                    if (suggested is Level.Suggested) {
//                        Text(
//                            "Level: ${suggested.level}",
//                            modifier = Modifier.padding(top = 8.dp),
//                            style = TextStyle(fontSize = 14.sp)
//                        )
//                    }
//
//                    val details = item.details
//
//                    if (expandedItems.contains(it)) {
//                        Column {
//                            details.forEach { detail ->
//                                Text(
//                                    detail.detail,
//                                    modifier = Modifier
//                                        .padding(vertical = 5.dp)
//                                        .fillMaxWidth()
//                                        .clickable { },
//                                    style = TextStyle(fontSize = 14.sp)
//                                )
//                            }
//                        }
//                    }
//
//                    if (details.isNotEmpty()) {
//                        Icon(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    onClick(it)
//                                },
//                            imageVector = if (expandedItems.contains(it)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                            contentDescription = null
//                        )
//                    }
//                }
//            }
//        }
//    }
//}


@Composable
fun <T> QuestGroups(
    items: List<T>,
    group: @Composable (T) -> Unit,
    showChildren: (T) -> Boolean,
    children: LazyListScope.(T) -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxWidth()
    ) {
        group(
            list = items,
            showChildren = showChildren,
            group = {
                group(it)
            },
            children = {
                children(it)
            }
        )
    }
}

fun <T> LazyListScope.group(
    list: List<T>,
    showChildren: (T) -> Boolean,
    group: @Composable (T) -> Unit,
    children: (T) -> Unit
) {
    list.forEach { item ->
        item {
            group(item)
        }
        if (showChildren(item)) {
            children(item)
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
