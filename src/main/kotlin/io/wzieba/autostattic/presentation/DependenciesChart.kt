package io.wzieba.autostattic.presentation

import io.kvision.chart.*
import io.kvision.core.Col
import io.kvision.core.Color
import io.wzieba.autostattic.domain.Record
import io.wzieba.autostattic.jsObject
import kotlin.js.Date

class DependenciesChart(records: List<Record>) : Chart(
        configuration = Configuration(
                type = ChartType.LINE,
                dataSets = listOf(
                        DataSets(
                                label = "Outdated dependencies",
                                data = records.map(Record::outdatedDependencies),
                        ),
                        DataSets(
                                label = "All dependencies",
                                data = records.map(Record::allDependencies)
                        )
                ),
                labels = records.map(Record::date).map(Date::toLabel),
                options = ChartOptions(
                        title = defaultTitleOptions("Dependencies"),
                        maintainAspectRatio = true,
                        scales = ChartScales(
                                yAxes = listOf(beginWithZeroScale)
                        ),
                        plugins = jsObject {
                            colorschemes = jsObject {
                                scheme = "office.Slipstream6"
                            }
                        },
                )
        )
)
