package com.jfranco.witcher3.quests.cli

import java.io.FileInputStream

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor

fun main() = xlsx()

fun dataFrame() {
    val repo = FileInputStream("/Users/f.sangiacomo/Downloads/quests.csv")
        .use(::CsvQuestsRepositoryImpl)

    val quests = repo.all()
    val json = Json.encodeToString(quests)
    println(json)

    File("/Users/f.sangiacomo/FranCode/witcher3_quests/androidApp/src/main/res/raw/quests.json")
        .writeText(json)
}

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    FileInputStream(File(filePath)).use { fis ->
        val workbook: Workbook = WorkbookFactory.create(fis)
        val sheet = workbook.getSheetAt(1)

        // Define the starting row and column range (A to G corresponds to 0 to 6 in zero-based indexing)
        val startRow = 9 // Row 10 (index 9)
        val startCol = 0 // Column A (index 0)
        val endCol = 6   // Column G (index 6)

        val theOnlyReasonMessage =
            "THE ONLY REASON THAT 'DESTINATION: SKELLIGE' IS PLACED HERE IS BECAUSE 'FLESH FOR SALE' IS EASILY MISSABLE AND THE ONLY WAY TO DO IT IS IN SKELLIGE. AS LONG AS YOU COMPLETE 'FLESH FOR SALE' BEFORE STARTING 'FOLLOWING THE THREAD', THEN YOU CAN TRAVEL TO SKELLIGE WHEN YOU ARE READY."

        val storyBranchMarker = "STORY BRANCH "
        val noOrderStartMarker = "THE FOLLOWING QUESTS CAN BE DONE AT ANY TIME IN ANY ORDER"
        val considerIgnoringNextMarker =
            "THE FOLLOWING QUEST IS NOT WORTH DOING CONSIDERING HOW OUT OF ORDER YOU WILL HAVE TO DO CERTAIN QUESTS."
        val theOnlyReasonMarker = "THE ONLY REASON THAT 'DESTINATION: SKELLIGE'"


        val skip = listOf(
            storyBranchMarker,
            noOrderStartMarker,
            considerIgnoringNextMarker,
            "IF YOU WOULD LIKE TO WATCH IT"
        )

        var anyOrder = false
        var consecutiveBlankRows = 0
        var considerIgnoringCounter = -1
        var theOnlyReasonCounter = -1

        var id: Int? = null

        var location: String? = null
        var questName: String? = null
        var questColor: String? = null
        var questLink: String? = null
        var storyBranch: String? = null
        var detail: String? = null
        var detailLink: String? = null

        val quests = mutableMapOf<QuestInfo, List<ExtraDetail>>()

        for (row in sheet) {

            if (row.rowNum >= startRow) { // Check if row is >= 10
                // Check if the entire row is blank
                if (isRowBlank(row)) {
                    consecutiveBlankRows++
                    if (consecutiveBlankRows == 2) {
                        anyOrder = false
                        storyBranch = null
                    }
                    continue // Skip processing this blank row
                } else {
                    consecutiveBlankRows = 0 // Reset counter if row is not blank
                }

                for (colIndex in startCol..endCol) {
                    val cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    val cellValue = getCellValue(cell)

                    // Detect start marker
                    if (cellValue == noOrderStartMarker) {
                        storyBranch = null
                        anyOrder = true
                        consecutiveBlankRows = 0
                        continue
                    } else if (cellValue?.contains(storyBranchMarker) == true) {
                        storyBranch = cellValue
                        continue
                    } else if (cellValue?.contains(considerIgnoringNextMarker) == true) {
                        considerIgnoringCounter = 1
                        continue
                    } else if (cellValue?.contains(theOnlyReasonMarker) == true) {
                        theOnlyReasonCounter = 7
                        continue
                    }

                    // Location
                    if (colIndex == 0) {
                        // Print marker status
                        if (anyOrder) {
                            print("*\t")
                        } else {
                            print(" \t")
                        }

                        if (storyBranch != null) {
                            val str = storyBranch.removePrefix(storyBranchMarker)
                            print("$str\t")
                        } else {
                            print(" \t")
                        }

                        if (cellValue != null) {
                            location = cellValue
                            print("$cellValue\t")
                        } else {
                            print("\t")
                        }
                    }

                    // Quest name
                    if (colIndex == 1) {
                        val cellColor = getCellColor(cell)
                        if (cellColor != null) {
                            print("$cellColor\t")
                            questColor = cellColor
                        }

                        val hyperlink = cell.hyperlink
                        if (hyperlink != null) {
                            print("${hyperlink.address}\t")
                            questLink = hyperlink.address
                        }

                        if (cellValue != null) {
                            id = row.rowNum
                            questName = cellValue
                            print("$cellValue\t")
                        } else {
                            print("\t")
                        }
                    }

                    // Details
                    if (colIndex == 3) {
                        if (cellValue != null) {
                            detail = cellValue
                            print("$cellValue\t")
                        } else {
                            print("\t")
                        }

                        val hyperlink = cell.hyperlink
                        if (hyperlink != null) {
                            print("${hyperlink.address}\t")
                            detailLink = hyperlink.address
                        }
                    }
                }

                println() // Move to the next line after printing all columns in the row

                val details = if (detail != null) {
                    mutableListOf(ExtraDetail(detail, detailLink, false))
                } else {
                    mutableListOf()
                }

                val msg =
                    if (theOnlyReasonCounter > 0 && theOnlyReasonCounter < 7) theOnlyReasonMessage else null

                val quest = QuestInfo(
                    id!!,
                    location!!,
                    questName!!,
                    questColor!!,
                    questLink!!,
                    anyOrder,
                    considerIgnoringCounter == 0,
                    storyBranch,
                    msg,
                )

                if (quests.contains(quest)) {
                    val existing = quests.getValue(quest)
                    val new = existing.plus(details)
                    quests[quest] = new
                } else {
                    quests[quest] = details
                }

                if (considerIgnoringCounter > 0) considerIgnoringCounter--
                else considerIgnoringCounter = -1

                if (theOnlyReasonCounter > 0) {
                    theOnlyReasonCounter--
                } else {
                    theOnlyReasonCounter = -1
                }
            }
        }

        quests.map {
            Quest(it.key, it.value)
        }.forEach {
            if (it.questInfo.anyOrder) {
                print("*\t")
            } else {
                print(" \t")
            }

            if (it.questInfo.storyBranch != null) {
                val str = it.questInfo.storyBranch.removePrefix(storyBranchMarker)
                print("$str\t")
            } else {
                print(" \t")
            }

            if (it.questInfo.considerIgnoringNext) {
                print("?\t")
            } else {
                print(" \t")
            }

            if (it.questInfo.theOnlyReasonMessage != null) {
                print("!\t")
            } else {
                print(" \t")
            }

            println("${it.questInfo.location}, ${it.questInfo.name}, ${it.questInfo.link}, ${it.questInfo.anyOrder}")
            it.extraDetails.forEach { d ->
                println("\t\t\t\t\t\t${d.detail}, ${d.link}")
            }
        }
    }
}

// Helper function to check if a row is completely blank
fun isRowBlank(row: Row): Boolean {
    for (cell in row) {
        if (getCellValue(cell)?.isNotBlank() == true) {
            return false
        }
    }
    return true
}

// Helper function to get the cell's background color as a hex string
fun getCellColor(cell: Cell): String? {
    return when (val cellStyle: CellStyle = cell.cellStyle) {
        is XSSFCellStyle -> { // For .xlsx files
            val xssfColor: XSSFColor? = cellStyle.fillForegroundColorColor
            xssfColor?.rgb?.joinToString("") { "%02x".format(it) }
        }

        else -> null
    }
}

// Helper function to get the value of a cell as a string
fun getCellValue(cell: Cell): String? {
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue
        CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
            cell.localDateTimeCellValue.toString()
        } else {
            cell.numericCellValue.toString()
        }

        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> cell.cellFormula
        else -> null
    }
}

data class Quest(
    val questInfo: QuestInfo,
    val extraDetails: List<ExtraDetail>,
)

data class QuestInfo(
    val id: Int,
    val location: String,
    val name: String,
    val color: String,
    val link: String,
    val anyOrder: Boolean,
    val considerIgnoringNext: Boolean,
    val storyBranch: String?,
    val theOnlyReasonMessage: String?,
)

data class ExtraDetail(
    val detail: String,
    val link: String?,
    val isCompleted: Boolean,
)