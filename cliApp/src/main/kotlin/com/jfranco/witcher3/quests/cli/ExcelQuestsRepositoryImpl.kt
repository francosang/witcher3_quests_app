package com.jfranco.witcher3.quests.cli

import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestsRepository

class ExcelQuestsRepositoryImpl(
    path: String
) : QuestsRepository {
    override fun all(): List<Quest> {
        return emptyList()
    }

}