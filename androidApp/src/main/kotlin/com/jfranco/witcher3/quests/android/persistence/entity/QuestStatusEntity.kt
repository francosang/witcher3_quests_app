package com.jfranco.witcher3.quests.android.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quest_status")
data class QuestStatusEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "is_hidden") val isHidden: Boolean,
)