package com.jfranco.witcher3.quests.cli

import java.io.FileInputStream

fun main() = xlsx()

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    val repo = FileInputStream(filePath)
        .use(::ExcelQuestsRepositoryImpl)

    val quests = repo.extractData()

    repo.write(
        quests,
        "/Users/f.sangiacomo/FranCode/witcher3_quests/androidApp/src/main/res/raw/quests.json"
    )
}