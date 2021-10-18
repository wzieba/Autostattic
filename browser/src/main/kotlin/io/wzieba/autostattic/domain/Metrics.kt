package io.wzieba.autostattic.domain

data class Metrics(
        val project: Project,
        val records: List<Record>
) {
}