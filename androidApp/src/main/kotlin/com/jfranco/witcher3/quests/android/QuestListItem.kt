package com.jfranco.witcher3.quests.android

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfranco.w3.quests.shared.ExtraDetail
import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.Type
import com.jfranco.witcher3.quests.android.theme.AppTheme

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
fun QuestListItem(
    quest: Quest,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (Quest) -> Unit,
) {
    val cardPadding = 16.dp
    val checkBoxPadding = 10.dp

    Card(
        modifier = modifier
            .padding(horizontal = cardPadding, vertical = 4.dp)
            .semantics { selected = isSelected }
            .clickable { onClick(quest) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(start = cardPadding)
                    .padding(top = cardPadding),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    QuestImage(description = "Main")
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = quest.type.type,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = "Level #",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Checkbox(
                        checked = quest.isCompleted,
                        onCheckedChange = { /*Check Implementation*/ },
                        modifier = Modifier.padding(end = checkBoxPadding)
                    )
                }

                Text(
                    text = quest.quest,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                )
            }

            val details = quest.extraDetails

            if (isSelected) {
                Column {
                    details.forEach { detail ->
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                alpha = 0.25F
                            )
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                detail.detail,
                                modifier = Modifier
                                    .weight(0.5F)
                                    .padding(cardPadding)
                                    .fillMaxWidth(),
                                style = TextStyle(fontSize = 14.sp)
                            )

                            Checkbox(
                                checked = detail.isCompleted,
                                modifier = Modifier
                                    .padding(end = checkBoxPadding)
                                    .scale(0.75F),
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
                        .padding(horizontal = cardPadding)
                        .padding(bottom = cardPadding)
                        .clickable {
                            onClick(quest)
                        },
                    imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
fun QuestListItemPreview() {
    val sampleQuest = Quest(
        quest = "Sample Quest",
        type = Type.Main,
        isCompleted = false,
        extraDetails = listOf(
            ExtraDetail("Detail 1", null, false),
            ExtraDetail("Detail 2", null, true)
        ),
        location = "Location",
        suggested = Level.Unaffected,
        url = "url",
        branch = null,
        order = Order.Suggested(1)
    )

    AppTheme {
        QuestListItem(
            quest = sampleQuest,
            isSelected = true,
            onClick = {}
        )
    }
}
