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


//        for (row in sheet) {
//            println(row)
//
//            for (cell in row) {
//                val style = cell.cellStyle
//
//                // Extract background color
//                val color = style.fillForegroundColorColor
//
//                println("Cell [${cell.rowIndex}, ${cell.columnIndex}] has style: $color")
//
//                if (color is XSSFColor) {
//                    val rgb = color.rgb
//                    if (rgb != null) {
//                        println(
//                            "Cell [${cell.rowIndex}, ${cell.columnIndex}] has RGB color: #${
//                                "%02x%02x%02x".format(rgb[0], rgb[1], rgb[2])
//                            }"
//                        )
//                    } else {
//                        println("Cell [${cell.rowIndex}, ${cell.columnIndex}] has no color set.")
//                    }
//                } else if (color is HSSFExtendedColor) {
//                    val rgb = color.rgb
//                    if (rgb != null) {
//                        println(
//                            "Cell [${cell.rowIndex}, ${cell.columnIndex}] has RGB color: #${
//                                "%02x%02x%02x".format(rgb[0], rgb[1], rgb[2])
//                            }"
//                        )
//                    } else {
//                        println("Cell [${cell.rowIndex}, ${cell.columnIndex}] has no color set.")
//                    }
//                } else {
//                    println("Cell [${cell.rowIndex}, ${cell.columnIndex}] uses a color format not supported by XSSF.")
//                }
//            }
//        }

fun xlsx() {
    val filePath = "/Users/f.sangiacomo/Downloads/excel.xlsx"

    FileInputStream(File(filePath)).use { fis ->
        val workbook: Workbook = WorkbookFactory.create(fis)
        val sheet = workbook.getSheetAt(1)

        // Define the starting row and column range (A to G corresponds to 0 to 6 in zero-based indexing)
        val startRow = 9 // Row 10 (index 9)
        val startCol = 0 // Column A (index 0)
        val endCol = 6   // Column G (index 6)

        val noOrderStartMarker = "THE FOLLOWING QUESTS CAN BE DONE AT ANY TIME IN ANY ORDER"
        var withinNoOrderMarker = false
        var consecutiveBlankRows = 0

        for (row in sheet) {
            if (row.rowNum >= startRow) { // Check if row is >= 10
                // Check if the entire row is blank
                if (isRowBlank(row)) {
                    consecutiveBlankRows++
                    if (consecutiveBlankRows == 2) {
                        withinNoOrderMarker = false
                    }
                    continue // Skip processing this blank row
                } else {
                    consecutiveBlankRows = 0 // Reset counter if row is not blank
                }

                // Print marker status
                if (withinNoOrderMarker) {
                    print("*\t")
                } else {
                    print(" \t")
                }

                for (colIndex in startCol..endCol) {
                    val cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    val cellValue = getCellValue(cell)

                    // Detect start marker
                    if (cellValue == noOrderStartMarker) {
                        withinNoOrderMarker = true
                        consecutiveBlankRows = 0
                    }

                    print("$cellValue\t")

                    if (colIndex == 1) {
                        val cellColor = getCellColor(cell)
                        if (cellColor != null) {
                            print("$cellColor\t")
                        }
                    }

                    // Check for hyperlinks in specific columns (B or D)
                    if (colIndex == 1 || colIndex == 3) {
                        val hyperlink = cell.hyperlink
                        if (hyperlink != null) {
                            print("${hyperlink.address}\t")
                        }
                    }
                }
                println() // Move to the next line after printing all columns in the row
            }
        }
    }
}

// Helper function to check if a row is completely blank
fun isRowBlank(row: Row): Boolean {
    for (cell in row) {
        if (getCellValue(cell).isNotBlank()) {
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
fun getCellValue(cell: Cell): String {
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue
        CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
            cell.localDateTimeCellValue.toString()
        } else {
            cell.numericCellValue.toString()
        }

        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> cell.cellFormula
        else -> ""
    }
}