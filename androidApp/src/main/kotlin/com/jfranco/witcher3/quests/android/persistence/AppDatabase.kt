package com.jfranco.witcher3.quests.android.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jfranco.witcher3.quests.android.persistence.dao.QuestStatusDao
import com.jfranco.witcher3.quests.android.persistence.entity.QuestStatusEntity

@Database(entities = [QuestStatusEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questStatusDao(): QuestStatusDao
}