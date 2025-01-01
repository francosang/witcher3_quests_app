package com.jfranco.witcher3.quests.android

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.jfranco.w3.quests.shared.QuestsRepository
import com.jfranco.witcher3.quests.android.persistence.AppDatabase
import com.jfranco.witcher3.quests.android.repo.JsonQuestsRepositoryImpl

class MainApp : Application() {

    lateinit var container: LazyContainer

    override fun onCreate() {
        super.onCreate()

        container = LazyContainer(this)
    }

}

class LazyContainer(
    private val context: Context
) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "w3_db"
        ).build()
    }

    val questsRepository: QuestsRepository by lazy {
        JsonQuestsRepositoryImpl(context, database.questStatusDao())
    }

}