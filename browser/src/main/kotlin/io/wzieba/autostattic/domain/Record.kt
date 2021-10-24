package io.wzieba.autostattic.domain

import kotlin.js.Date

data class Record(
        val date: Date,
        val outdatedDependencies: Int,
        val allDependencies: Int,
        val kotlinLines: Int?,
        val javaLines: Int?,
        val compilerWarnings: Int?,
)