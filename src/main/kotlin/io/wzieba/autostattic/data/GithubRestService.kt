package io.wzieba.autostattic.data

import io.kvision.rest.RestClient
import io.kvision.rest.callDynamic
import io.wzieba.autostattic.domain.Project
import io.wzieba.autostattic.domain.Record
import kotlinx.browser.window
import kotlinx.coroutines.await

private const val API_URL = "https://api.github.com/repos/wzieba/autostattic/contents"

class GithubRestService {
    private val restClient = RestClient()
    private val csvParser = CsvParser()

    suspend fun getStatisticsFor(project: Project): List<Record> {
        return restClient.callDynamic("$API_URL/${project.repositoryName}")
                .then<String> { githubResponse: dynamic ->
                    val content = githubResponse.content as String
                    window.atob(content)
                }.then { csv: String ->
                    csvParser.parse(csv)
                }.await()
    }
}
