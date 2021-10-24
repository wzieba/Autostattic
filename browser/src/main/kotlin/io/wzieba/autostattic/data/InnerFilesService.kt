package io.wzieba.autostattic.data

import io.wzieba.autostattic.domain.Project
import io.wzieba.autostattic.domain.Record
import kotlin.js.Promise
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Response

class InnerFilesService {
    private val csvParser = CsvParser()

    suspend fun getStatisticsFor(project: Project): List<Record> {
        return window.fetch("./${project.repositoryName}").unsafeCast<Promise<Response>>().then { response ->
            response.text()
        }.then { csv: String ->
            csvParser.parse(csv)
        }.await()
    }
}
