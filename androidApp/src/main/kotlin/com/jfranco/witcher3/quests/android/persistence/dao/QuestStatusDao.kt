package com.jfranco.witcher3.quests.android.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfranco.witcher3.quests.android.persistence.entity.QuestStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestStatusDao {
    @Query("SELECT * FROM quest_status")
    fun changes(): Flow<List<QuestStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg questStatus: QuestStatusEntity)
}