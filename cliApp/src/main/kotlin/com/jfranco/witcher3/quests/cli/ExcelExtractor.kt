package com.jfranco.witcher3.quests.cli

import com.jfranco.w3.quests.shared.ExtraDetail
import com.jfranco.w3.quests.shared.Level
import com.jfranco.w3.quests.shared.Order
import com.jfranco.w3.quests.shared.Quest
import com.jfranco.w3.quests.shared.QuestType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import java.io.File
import java.io.InputStream

// Define the starting row and column range (A to G corresponds to 0 to 6 in zero-based indexing)
const val startRow = 9 // Row 10 (index 9)

const val skelligeMessage =
    "THE ONLY REASON THAT 'DESTINATION: SKELLIGE' IS PLACED HERE IS BECAUSE 'FLESH FOR SALE' IS EASILY MISSABLE AND THE ONLY WAY TO DO IT IS IN SKELLIGE. AS LONG AS YOU COMPLETE 'FLESH FOR SALE' BEFORE STARTING 'FOLLOWING THE THREAD', THEN YOU CAN TRAVEL TO SKELLIGE WHEN YOU ARE READY."

const val considerIgnoringMessage =
    "THE FOLLOWING QUEST IS NOT WORTH DOING CONSIDERING HOW OUT OF ORDER YOU WILL HAVE TO DO CERTAIN QUESTS. IT IS SIMPLY A 3 MINUTE BIT OF DIALOGUE AND THAT IS IT. IF YOU WOULD LIKE TO WATCH IT, I'VE ATTACHED A YOUTUBE CLIP OF THE QUEST ITSELF IN THE EXTRA DETAILS LINK. I'VE ONLY ADDED IT HERE IN CASE SOMEONE DID WANT TO DO IT."

const val storyBranchMarker = "STORY BRANCH "
const val anyOrderStartMarker = "THE FOLLOWING QUESTS CAN BE DONE AT ANY TIME IN ANY ORDER"
const val considerIgnoringMarker =
    "THE FOLLOWING QUEST IS NOT WORTH DOING CONSIDERING HOW OUT OF ORDER YOU WILL HAVE TO DO CERTAIN QUESTS."
const val theOnlyReasonMarker = "THE ONLY REASON THAT 'DESTINATION: SKELLIGE'"

val missionTypeByColor = mapOf(
    "e06666" to QuestType.Main,
    "f6b26b" to QuestType.Secondary,
    "ffd966" to QuestType.Contract,
    "93c47d" to QuestType.TreasureHunt,
    "3d85c6" to QuestType.ScavengerHunt,
    "a4c2f4" to QuestType.GwentAndTheHeroesPursuits,
    "8e7cc3" to QuestType.ChanceEncounters,
)

interface DataExtractor {
    fun extractData(): List<Quest>
    fun write(data: List<Quest>, destination: String)
}

class ExcelExtractor(
    inputStream: InputStream
) : DataExtractor {

    private val workbook: Workbook = WorkbookFactory.create(inputStream)

    override fun extractData(): List<Quest> {
        val sheet = workbook.getSheetAt(1)

        val quests = mutableMapOf<QuestKey, QuestValue>()

        // these are the field values of the quests, they are updated while iterating the sheet
        var previousLocation: String? = null
        var previousName: String? = null
        var message: String? = null
        var previousLink: String? = null
        var previousColor: String? = null

        // These values are control flags for markers, they are handled different than field values
        var anyOrder = false
        var considerIgnoring = false
        var emptyRowCount = 0
        var storyBranch: String? = null

        for (row in sheet.iterator().asSequence()
            .filter { it.rowNum >= startRow }) {

            if (row.containsString(anyOrderStartMarker)) {
                anyOrder = true
                continue
            } else if (row.containsString(theOnlyReasonMarker)) {
                message = skelligeMessage
                continue
            } else if (row.containsString(considerIgnoringMarker)) {
                considerIgnoring = true
                message = considerIgnoringMessage
                continue
            } else if (row.containsString(storyBranchMarker)) {
                storyBranch = row.getCell(0).stringCellValue
                continue
            } else if (isRowBlank(row)) {
                emptyRowCount++
                if (emptyRowCount == 2) {
                    considerIgnoring = false
                    anyOrder = false
                    message = null
                    storyBranch = null
                }
                continue
            }
            emptyRowCount = 0

            val location = row.getValueAt(0) ?: previousLocation
            val name = row.getValueAt(1) ?: previousName
            val link = row.getLinkAddressAt(1) ?: previousLink
            val color = row.getColorAt(1) ?: previousColor
            val extra = row.getValueAt(3)?.let { listOf(ExtraDetail(it)) } ?: emptyList()

            // println("${(location ?: "").padEnd(5)} ${(name ?: "").padEnd(10)}, ${extra.firstOrNull()}")

            if (location != null && name != null) {
                val (quest, level) = extractLevel(name)

                val key = QuestKey(
                    location = location,
                    name = quest,
                    link = link!!,
                    level = level,
                    order = if (anyOrder) Order.Any else Order.Suggested(0),
                    color = color!!,
                    type = missionTypeByColor[color]!!,
                    message = message,
                    storyBranch = storyBranch,
                    considerIgnoring = considerIgnoring
                )

                if (quests.contains(key)) {
                    val questValue = quests[key]!!
                    quests[key] = questValue.copy(extras = questValue.extras + extra)
                } else {
                    quests[key] = QuestValue(
                        id = row.rowNum + 1,
                        extras = extra
                    )
                }

                previousLocation = location
                previousName = name
                previousLink = link
                previousColor = color
            } else if (extra.isNotEmpty() && quests.isNotEmpty()) {
                throw RuntimeException("Why am I here?")
            }
        }
        return quests.map { (key, value) ->
            Quest(
                id = value.id,
                type = key.type,
                location = key.location,
                quest = key.name,
                color = key.color,
                isCompleted = false,
                url = key.link,
                suggested = key.level,
                order = key.order,
                extraDetails = value.extras,
                considerIgnoring = key.considerIgnoring,
                branch = key.storyBranch,
                message = key.message,
            )
        }
    }

    override fun write(data: List<Quest>, destination: String) {
        val json = Json.encodeToString(data)
        File(destination).writeText(json)
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

fun Row.containsString(str: String): Boolean {
    for (cell in this) {
        val cellValue = getCellValue(cell)
        if (cellValue.isNullOrBlank()) return false

        if (cellValue.contains(str)) {
            return true
        }
    }
    return false
}


// Helper function to get the cell's background color as a hex string
fun getCellColor(cell: Cell): String? {
    return when (val cellStyle: CellStyle = cell.cellStyle) {
        is XSSFCellStyle -> { // For .xlsx files
            val xssfColor: XSSFColor? =
                cellStyle.fillBackgroundColorColor // fillForegroundColorColor
            xssfColor?.rgb?.joinToString("") { "%02x".format(it) }
        }

        else -> null
    }
}

fun Row.getValueAt(int: Int): String? {
    val cell = getCell(int, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
    return getCellValue(cell)
}

fun Row.getLinkAddressAt(int: Int): String? {
    val cell = getCell(int, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
    return cell?.hyperlink?.address
}

fun Row.getColorAt(int: Int): String? {
    val cell = getCell(int, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
    return getCellColor(cell)
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

private fun extractLevel(input: String): Pair<String, Level> {
    val regex = "^(.*?)\\s*(?:\\((\\d+)\\))?$".toRegex()
    val matchResult = regex.find(input)
    val firstPart = matchResult?.groups?.get(1)?.value ?: ""
    val level = matchResult?.groups?.get(2)?.value?.toInt()
    val levelObj = if (level == null) Level.Any else Level.Suggested(level)
    return Pair(firstPart, levelObj)
}

data class QuestKey(
    val type: QuestType,
    val location: String,
    val name: String,
    val level: Level,
    val order: Order,
    val color: String,
    val link: String,
    val considerIgnoring: Boolean,
    val storyBranch: String?,
    val message: String?,
)

private data class QuestValue(
    val id: Int,
    val extras: List<ExtraDetail>,
)