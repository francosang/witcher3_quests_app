package com.jfranco.witcher3.quests.android.repo

import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestsRepository
import kotlinx.serialization.json.Json

class JsonQuestsRepositoryImpl(
    private val content: String
) : QuestsRepository {

    override fun all(): List<Quest> {
        return Json.decodeFromString<List<Quest>>(content)
    }
}