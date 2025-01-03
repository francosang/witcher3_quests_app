package com.jfranco.witcher3.quests.cli

import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Quest
import java.io.FileInputStream

fun main() = xlsx()

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    val repo = FileInputStream(filePath)
        .use(::ExcelExtractor)

    val quests = repo.extractData()

    quests
        .groupBy { it.id }
        .filter { it.value.size > 1 }
        .forEach { g ->
            println("> Quest with id ${g.key} is duplicated: ${g.value.size} times")
            g.value.forEach {
                println("----- (${it.extraDetails.size}) - ${it.quest}")
            }
        }

    if (quests.groupBy { it.hashCode() }.any { it.value.size > 1 }) {
        throw IllegalStateException("There are duplicated quests")
    }

    fun validateQuest(
        name: String,
        id: Int,
        link: String,
        level: Level,
        extrasSize: Int,
        message: String?,
    ) {
        val filtered = quests.filter { it.quest == name }
        if (filtered.size != 1)
            throw IllegalStateException("Expected only one quests with name $name, but found ${filtered.size}")

        filtered.forEach {
            if (it.id != id) {
                println(it)
                throw IllegalStateException("Quest has id ${it.id}, but expected $id")
            }

            if (it.url != link) {
                println(it)
                throw IllegalStateException("Quest has link ${it.url}, but expected $link")
            }

            if (it.suggested != level) {
                println(it)
                throw IllegalStateException("Quest has level ${it.suggested}, but expected $level")
            }

            if (it.message != message)
                throw IllegalStateException("Quest has message ${it.message}, but expected $message")

            if (it.extraDetails.size != extrasSize) {
                val msg =
                    "Quest has ${it.extraDetails.size} extra details, but expected $extrasSize"
                println(msg)
                it.extraDetails.forEach { d -> println("\t$d") }
                throw IllegalStateException(msg)
            }
        }
    }

    fun validateQuest(
        names: List<Triple<String, Int, String?>>,
        link: String,
        level: Level,
        extrasSize: Int,
    ) {
        names.forEach { (name, id, storyBranch) ->
            val filtered = quests.filter { it.quest == name && it.id == id }
            if (filtered.size != 1)
                throw IllegalStateException("Expected only one quests with name $name, but found ${filtered.size}")

            filtered.forEach {
                if (it.id != id) {
                    println(it)
                    throw IllegalStateException("Quest has id ${it.id}, but expected $id")
                }

                if (it.url != link) {
                    println(it)
                    throw IllegalStateException("Quest has link ${it.url}, but expected $link")
                }

                if (it.suggested != level) {
                    println(it)
                    throw IllegalStateException("Quest has level ${it.suggested}, but expected $level")
                }

                if (it.branch != storyBranch)
                    throw IllegalStateException("Quest has message ${it.branch}, but expected $storyBranch")

                if (it.extraDetails.size != extrasSize) {
                    val msg =
                        "Quest has ${it.extraDetails.size} extra details, but expected $extrasSize"
                    println(msg)
                    it.extraDetails.forEach { d -> println("\t$d") }
                    throw IllegalStateException(msg)
                }
            }
        }
    }

    validateQuest(
        "Kaer Morhen", 10,
        "https://witcher.fandom.com/wiki/Kaer_Morhen_(quest)?so=search",
        Level.Suggested(1), extrasSize = 9, message = null
    )
    validateQuest(
        "Destination: Skellige", 281,
        "https://witcher.fandom.com/wiki/Destination:_Skellige?so=search",
        Level.Suggested(16), extrasSize = 4, message = skelligeMessage
    )
    validateQuest(
        "The Mysterious Passenger", 97,
        "https://witcher.fandom.com/wiki/A_Mysterious_Passenger",
        Level.Suggested(1), extrasSize = 1, message = considerIgnoringMessage
    )
    validateQuest(
        "Gwent: To Everything - Turn, Turn, Tournament!", 982,
        "https://witcher.fandom.com/wiki/Gwent:_To_Everything_-_Turn,_Turn,_Tournament!?so=search",
        Level.Suggested(38), extrasSize = 2, message = null
    )
    validateQuest(
        listOf(
            Triple("Be It Ever So Humble...", 897, "STORY BRANCH 1"),
            Triple("Be It Ever So Humble...", 915, "STORY BRANCH 2")
        ),
        "https://witcher.fandom.com/wiki/Be_It_Ever_So_Humble...?so=search",
        Level.Suggested(49), extrasSize = 4,
    )

    repo.write(
        quests,
        "/Users/f.sangiacomo/FranCode/witcher3_quests/androidApp/src/main/res/raw/quests.json"
    )
}

