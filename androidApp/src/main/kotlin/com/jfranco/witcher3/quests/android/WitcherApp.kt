package com.jfranco.witcher3.quests.android

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestStatus
import com.jfranco.w3.quests.shared.QuestsCollection
import com.jfranco.witcher3.quests.android.persistence.AppDatabase
import com.jfranco.witcher3.quests.android.repo.JsonQuestsRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class DiContainer {

    data class Ready(
        val appContext: Context,
        val questsRepository: QuestsRepository
    ) : DiContainer()

    data object Loading : DiContainer()

}

@Composable
fun LoadDiContainer() {
    var diContainer by remember { mutableStateOf<DiContainer>(DiContainer.Loading) }

    when (val di = diContainer) {
        is DiContainer.Loading -> {
            val context = LocalContext.current
            LaunchedEffect(diContainer) {
                diContainer = buildDi(context)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Loading app...")
                CircularProgressIndicator()
            }
        }

        is DiContainer.Ready -> WitcherApp(di)

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WitcherApp(di: DiContainer.Ready) {
    val coroutineScope = rememberCoroutineScope()
    val questsByLocation by di.questsRepository.updates().collectAsStateWithLifecycle(emptyList())

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
            questsByLocation
                .forEach { collection ->
                    when (collection) {
                        is QuestsCollection.QuestsByLocation -> {
                            stickyHeader {
                                Surface(Modifier.fillParentMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
                                            .wrapContentHeight()
                                    ) {
                                        Text(
                                            text = collection.location(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp)
                                                .padding(top = 8.dp),
                                            style = MaterialTheme.typography.headlineSmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            items(collection.quests) {
                                QuestCard(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    quest = it,
                                    isSelected = expandedItems.contains(it),
                                    onClick = ::onClick,
                                    onCompletedChanged = { isChecked ->
                                        Log.i(
                                            "MainActivity",
                                            "Checked: $isChecked"
                                        )

                                        coroutineScope.launch {
                                            di.questsRepository.save(
                                                QuestStatus(
                                                    id = it.id,
                                                    isCompleted = isChecked,
                                                    isHidden = false
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        is QuestsCollection.QuestsGrouped -> {

                            collection.questsGroups.forEach { (groupName, quests) ->
                                stickyHeader {
                                    Surface(Modifier.fillParentMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                        ) {
                                            Text(
                                                text = collection.location(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(6.dp)
                                                    .padding(top = 8.dp),
                                                style = MaterialTheme.typography.headlineSmall,
                                                textAlign = TextAlign.Center
                                            )

                                            Text(
                                                text = groupName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                items(quests) {
                                    QuestCard(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        quest = it,
                                        isSelected = expandedItems.contains(it),
                                        onClick = ::onClick,
                                        onCompletedChanged = { isChecked ->
                                            Log.i(
                                                "MainActivity",
                                                "Checked: $isChecked"
                                            )

                                            coroutineScope.launch {
                                                di.questsRepository.save(
                                                    QuestStatus(
                                                        id = it.id,
                                                        isCompleted = isChecked,
                                                        isHidden = false
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}


suspend fun buildDi(context: Context) = withContext(Dispatchers.IO) {
    val appContext = context.applicationContext

    val inputStream = appContext.resources.openRawResource(R.raw.quests)

    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "w3_db"
    ).build()

    val questsRepository = JsonQuestsRepositoryImpl(inputStream, db.questStatusDao())
    DiContainer.Ready(appContext, questsRepository)
}