package io.wzieba.autostattic.presentation

import io.kvision.chart.*
import io.kvision.core.Col
import io.kvision.core.Color
import io.wzieba.autostattic.domain.Record
import io.wzieba.autostattic.jsObject

class LanguageRatioChart(records: List<Record>) : Chart(
        configuration = Configuration(
                type = ChartType.LINE,
                dataSets = listOf(
                        DataSets(
                                label = "Java",
                                data = getJavaPercentages(records),
                        ),
                        DataSets(
                                label = "Kotlin",
                                data = getKotlinPercentages(records)
                        ),
                        DataSets(
                                label = "Combined",
                                data = getAllLines(records)
                        )
                ),
                labels = getLabels(records),
                options = ChartOptions(
                        title = defaultTitleOptions("Java to Kotlin lines of code ratio"),
                        maintainAspectRatio = true,
                        scales = ChartScales(
                                yAxes = listOf(beginWithZeroScale)
                        ),
                        plugins = jsObject {
                            colorschemes = jsObject {
                                scheme = "brewer.RdYlBu11"
                            }
                        }
                ),
        ),
)

private fun getLabels(records: List<Record>): List<String> {
    return getApplicableRecords(records).map { it.date.toLabel() }
}

private fun getJavaPercentages(records: List<Record>): List<Int> {
    return getApplicableRecords(records).map { record ->
        record.javaLines ?: 0
    }
}

private fun getKotlinPercentages(records: List<Record>): List<Int> {
    return getApplicableRecords(records).map { record ->
        record.kotlinLines ?: 0
    }
}

private fun getAllLines(records: List<Record>): List<Int> {
    return getApplicableRecords(records).map { record ->
        (record.kotlinLines ?: 0) + (record.javaLines ?: 0)
    }
}

private fun getApplicableRecords(records: List<Record>) = records
        .filter {
            it.kotlinLines != null || it.javaLines != null
        }