package io.wzieba.autostattic.presentation

import io.kvision.chart.TitleOptions
import io.kvision.types.toStringF
import io.wzieba.autostattic.jsObject
import kotlin.js.Date

val beginWithZeroScale = jsObject {
    ticks = jsObject {
        beginAtZero = true
    }
}

fun Date.toLabel(): String {
    return this.toStringF("DD MMM YY")
}

fun defaultTitleOptions(text: String) = TitleOptions(
        display = true,
        fontSize = 22,
        text = listOf(text)
)
