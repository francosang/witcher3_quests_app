package com.jfranco.witcher3.quests.cli

import com.jfranco.w3.quests.shared.ExtraDetail
import com.jfranco.w3.quests.shared.Order
import java.io.FileInputStream

fun main() = xlsx()

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    val repo = FileInputStream(filePath)
        .use(::ExcelExtractor)

//    val sheet = listOf(
//        listOf("kaer", "ciri", null),
//        listOf("white", "fire", "d1"),
//        listOf(null, null, "d2"),
//        listOf(null, "inci", "d3"),
//        listOf("any"),
//        listOf("white", "card", null),
//        listOf(),
//        listOf(),
//        listOf("velen", "blood", "d5"),
//    )
    val quests = repo.extractSheet()
    quests.forEach { println(it) }

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

    fun validateQuest(name: String, id: Int, extrasSize: Int, message: String?) {
        if (quests.filter { it.name.contains(name) }.map {
                if (it.id != id) throw IllegalStateException("Quest has id ${it.id}, but expected $id")

//                if (message != null)
//                    throw IllegalStateException("Quest has message ${message}, but expected null")

                if (it.extraDetails.size != extrasSize) {
                    val msg =
                        "Quest has ${it.extraDetails.size} extra details, but expected $extrasSize"
                    println(msg)
                    it.extraDetails.forEach { d -> println("\t$d") }
                    throw IllegalStateException(msg)
                }
                it
            }.groupBy { it.name }.any { it.value.size > 1 })
            throw IllegalStateException("$name is duplicated")

    }

    validateQuest("Kaer Morhen", id = 10, extrasSize = 9, message = null)
    validateQuest("Destination: Skellige", id = 281, extrasSize = 4, message = theOnlyReasonMessage)

//    val quests = repo.extractData()
//
//    repo.write(
//        quests,
//        "/Users/f.sangiacomo/FranCode/witcher3_quests/androidApp/src/main/res/raw/quests.json"
//    )
}