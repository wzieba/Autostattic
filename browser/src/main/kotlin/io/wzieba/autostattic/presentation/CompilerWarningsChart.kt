package io.wzieba.autostattic.presentation

import io.kvision.chart.Chart
import io.kvision.chart.ChartOptions
import io.kvision.chart.ChartScales
import io.kvision.chart.ChartType
import io.kvision.chart.Configuration
import io.kvision.chart.DataSets
import io.wzieba.autostattic.domain.Record
import io.wzieba.autostattic.jsObject
import kotlin.js.Date

class CompilerWarningsChart(records: List<Record>) : Chart(
    configuration = Configuration(
        type = ChartType.LINE,
        dataSets = listOf(
            DataSets(
                label = "Kotlin compiler warnings",
                data = getApplicableRecords(records).map(Record::compilerWarnings)
            ),
            DataSets(
                label = "Kotlin compiler warnings per 1000 lines",
                data = warningsPer1000Lines(getApplicableRecords(records)),
                yAxisID = "per-lines"
            )
        ),
        labels = getApplicableRecords(records).map(Record::date).map(Date::toLabel),
        options = ChartOptions(
            title = defaultTitleOptions("Compiler warnings"),
            maintainAspectRatio = true,
            scales = ChartScales(
                yAxes = listOf(
                    beginWithZeroScale,
                    jsObject {
                        id = "per-lines"
                        position = "right"
                        ticks = jsObject {
                            min = 0
                            suggestedMax = 10
                        }
                    }
                )
            ),
            plugins = jsObject {
                colorschemes = jsObject {
                    scheme = "brewer.DarkTwo3"
                }
            },
        )
    )
)

private fun getApplicableRecords(records: List<Record>) = records
    .filter {
        it.compilerWarnings != null
    }

private fun warningsPer1000Lines(records: List<Record>) = records.map {
    (((it.compilerWarnings?.toFloat() ?: 0f) * 1000f) / (it.kotlinLines?:0))
}
