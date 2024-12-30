package com.jfranco.witcher3.quests.cli

import java.io.FileInputStream

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor

fun main() = xlsx()

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    val repo = FileInputStream(filePath)
        .use(::ExcelQuestsRepositoryImpl)

    val quests = repo.all()
    val json = Json.encodeToString(quests)
    println(json)

    File("/Users/f.sangiacomo/FranCode/witcher3_quests/androidApp/src/main/res/raw/quests.json")
        .writeText(json)
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