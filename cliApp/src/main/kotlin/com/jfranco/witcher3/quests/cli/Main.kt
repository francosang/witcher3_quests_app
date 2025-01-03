package com.jfranco.witcher3.quests.cli

import com.jfranco.w3.quests.shared.Level
import java.io.FileInputStream

fun main() = xlsx()

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    val repo = FileInputStream(filePath)
        .use(::ExcelExtractor)

    val quests = repo.extractSheet()

    quests
        .groupBy { it.id }
        .filter { it.value.size > 1 }
        .forEach { g ->
            println("> Quest with id ${g.key} is duplicated: ${g.value.size} times")
            g.value.forEach {
                println("----- (${it.extraDetails.size}) - ${it.name}")
            }
        }

    if (quests.groupBy { it.hashCode() }.any { it.value.size > 1 }) {
        throw IllegalStateException("There are duplicated quests")
    }

    fun validateQuest(name: String, id: Int, level: Level, extrasSize: Int, message: String?) {
        val filtered = quests.filter { it.name == name }
        if (filtered.isEmpty())
            throw IllegalStateException("$name not found")

        if (filtered.groupBy { it.name }.any { it.value.size > 1 })
            throw IllegalStateException("$name is not unique")

        filtered.forEach {
            if (it.id != id) {
                println(it)
                throw IllegalStateException("Quest has id ${it.id}, but expected $id")
            }

            if (it.level != level) {
                println(it)
                throw IllegalStateException("Quest has level ${it.level}, but expected $level")
            }

            if (it.message != message)
                throw IllegalStateException("Quest has message ${message}, but expected null")

            if (it.extraDetails.size != extrasSize) {
                val msg =
                    "Quest has ${it.extraDetails.size} extra details, but expected $extrasSize"
                println(msg)
                it.extraDetails.forEach { d -> println("\t$d") }
                throw IllegalStateException(msg)
            }
        }
    }

    validateQuest("Kaer Morhen", 10, Level.Suggested(1), extrasSize = 9, message = null)
    validateQuest(
        "Destination: Skellige",
        281,
        Level.Suggested(16),
        extrasSize = 4,
        message = skelligeMessage
    )

    validateQuest(
        "The Mysterious Passenger",
        97,
        Level.Suggested(1),
        extrasSize = 1,
        message = considerIgnoringMessage
    )

//    val quests = repo.extractData()
//
//    repo.write(
//        quests,
//        "/Users/f.sangiacomo/FranCode/witcher3_quests/androidApp/src/main/res/raw/quests.json"
//    )
}