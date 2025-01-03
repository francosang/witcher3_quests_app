package com.jfranco.witcher3.quests.android.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jfranco.w3.quests.shared.ExtraDetail
import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestType
import com.jfranco.witcher3.quests.android.ui.theme.AppTheme

@Composable
fun QuestImage(
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary),
    )

//    Image(
//        modifier = modifier
//            .size(40.dp)
//            .clip(CircleShape),
//        painter = painterResource(id = drawableResource),
//        contentDescription = description,
//    )
}

@Composable
fun QuestCard(
    modifier: Modifier = Modifier,
    quest: Quest,
    isSelected: Boolean = false,
    onClick: (Quest) -> Unit,
    onCompletedChanged: (Boolean) -> Unit
) {
    Card(
        modifier = modifier
            .semantics { selected = isSelected }
            .clickable { onClick(quest) }
            .fillMaxWidth(),
    ) {
        val isBigCard = if (quest.isCompleted) {
            isSelected
        } else {
            true
        }

        val contentPadding = 16.dp
        val contentVerticalPadding = if (isBigCard) 16.dp else 8.dp

        Row(
            modifier = Modifier
                .padding(horizontal = contentPadding)
                .padding(vertical = contentVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBigCard) {
                QuestImage(description = "Main")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (isBigCard) {
                    Column(
                        Modifier.padding(start = contentPadding / 2)
                    ) {
                        Text(
                            text = quest.type.show,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "Level: ${quest.suggested.show()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    Text(
                        text = quest.quest,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                Checkbox(
                    checked = quest.isCompleted,
                    onCheckedChange = { onCompletedChanged(it) },
                )
            }
        }

        if (isBigCard) {
            Text(
                text = quest.quest,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = contentPadding)
                    .fillMaxWidth(),
            )

            val details = quest.extraDetails

            if (isSelected) {
                Column(
                    Modifier.padding(top = contentPadding)
                ) {
                    details.forEach { detail ->
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                alpha = 0.25F
                            )
                        )

                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = contentPadding)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                detail.detail,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = contentPadding / 2),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
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
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(
                            alpha = 0.25F
                        )
                    )
                }
            }

            Icon(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        onClick(quest)
                    },
                imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Preview(name = "Quests Completed")
@Composable
fun QuestsCompletedPreview() {
    val sampleQuest = Quest(
        id = 1,
        quest = "Sample Quest",
        isCompleted = true,
        extraDetails = listOf(
            ExtraDetail("Detail 1", null, false),
            ExtraDetail("Detail 2", null, true)
        ),
        location = "Location",
        suggested = Level.Any,
        url = "url",
        branch = null,
        order = Order.Suggested(1),
        color = "",
        considerIgnoring = false,
        message = null,
        type = QuestType.Main
    )
    AppTheme(
        useDarkTheme = false
    ) {
        Column {
            QuestCard(
                quest = sampleQuest,
                isSelected = false,
                onClick = {},
                onCompletedChanged = {}
            )
            Spacer(modifier = Modifier.size(8.dp))
            QuestCard(
                quest = sampleQuest,
                isSelected = true,
                onClick = {},
                onCompletedChanged = {}
            )
        }
    }
}


@Preview(name = "Quests Pending")
@Composable
fun QuestsOpenPreview() {
    val sampleQuest = Quest(
        id = 1,
        quest = "Sample Quest",
        isCompleted = false,
        extraDetails = listOf(
            ExtraDetail("Detail 1", null, false),
            ExtraDetail("Detail 2", null, true)
        ),
        location = "Location",
        suggested = Level.Any,
        url = "url",
        branch = null,
        order = Order.Suggested(1),
        color = "",
        considerIgnoring = false,
        message = null,
        type = QuestType.Main
    )

    AppTheme(
        useDarkTheme = false
    ) {
        Column {
            QuestCard(
                quest = sampleQuest,
                isSelected = false,
                onClick = {},
                onCompletedChanged = {}
            )
            Spacer(modifier = Modifier.size(8.dp))
            QuestCard(
                quest = sampleQuest,
                isSelected = true,
                onClick = {},
                onCompletedChanged = {}
            )
        }
    }
}