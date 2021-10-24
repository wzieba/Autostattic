package io.wzieba.autostattic.presentation

import io.kvision.chart.*
import io.wzieba.autostattic.domain.Record
import io.wzieba.autostattic.jsObject

class LanguageRatioChart(records: List<Record>) : Chart(
        configuration = Configuration(
                type = ChartType.LINE,
                dataSets = listOf(
                        DataSets(
                                label = "Java",
                                data = getJavaLines(records),
                        ),
                        DataSets(
                                label = "Kotlin",
                                data = getKotlinLines(records),
                        ),
                ),
                labels = getLabels(records),
                options = ChartOptions(
                        title = defaultTitleOptions("Lines of code"),
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

private fun getLabels(records: List<Record>) = getApplicableRecords(records).map { it.date.toLabel() }

private fun getJavaLines(records: List<Record>) = getApplicableRecords(records).map { record ->
    record.javaLines ?: 0
}

private fun getKotlinLines(records: List<Record>) = getApplicableRecords(records).map { record ->
    record.kotlinLines ?: 0
}

private fun getApplicableRecords(records: List<Record>) = records
        .filter {
            it.kotlinLines != null || it.javaLines != null
        }