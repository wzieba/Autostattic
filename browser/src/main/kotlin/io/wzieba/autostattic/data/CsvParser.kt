package io.wzieba.autostattic.data

import io.wzieba.autostattic.domain.Record
import kotlin.js.Date

class CsvParser {

    fun parse(rawCsv: String): List<Record> {
        return rawCsv.split("\n")
                .drop(1)
                .mapNotNull { rawLine ->
                    val rawValues = rawLine.split(",")
                    try {
                        Record(
                                date = rawValues[DATE_INDEX].toDate(),
                                outdatedDependencies = rawValues[OUTDATED_DEPENDENCIES_INDEX].toInt(),
                                allDependencies = rawValues[ALL_DEPENDENCIES_INDEX].toInt(),
                                kotlinLines = rawValues.getOrNull(KOTLIN_LINES_INDEX)?.toIntOrNull(),
                                javaLines = rawValues.getOrNull(JAVA_LINES_INDEX)?.toIntOrNull(),
                                compilerWarnings = rawValues.getOrNull(COMPILER_WARNINGS_INDEX)?.toIntOrNull()
                        )
                    } catch (exception: Exception) {
                        console.log(exception)
                        null
                    }
                }
    }

    private fun String.toDate(): Date {
        return split("/")
                .map(String::toInt)
                .let {
                    Date(
                            it[2],
                            it[1],
                            it[0],
                    )
                }
    }

    private companion object {
        const val DATE_INDEX = 0
        const val OUTDATED_DEPENDENCIES_INDEX = 1
        const val ALL_DEPENDENCIES_INDEX = 2
        const val KOTLIN_LINES_INDEX = 3
        const val JAVA_LINES_INDEX = 4
        const val COMPILER_WARNINGS_INDEX = 5
    }
}