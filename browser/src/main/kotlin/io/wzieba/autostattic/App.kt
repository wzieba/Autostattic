package io.wzieba.autostattic

import io.kvision.*
import io.kvision.html.*
import io.kvision.navbar.*
import io.kvision.navbar.nav
import io.kvision.panel.*
import io.wzieba.autostattic.data.GithubRestService
import io.wzieba.autostattic.domain.Project
import io.wzieba.autostattic.presentation.DependenciesChart
import io.wzieba.autostattic.presentation.LanguageRatioChart
import io.wzieba.autostattic.presentation.ViewState
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

val AppScope = CoroutineScope(window.asCoroutineDispatcher())
val service = GithubRestService()
val state = ViewState()

class App : Application() {

    override fun start() {
        root("kvapp") {
            navbar("Autostattic") {
                nav {
                    Project.values().forEach { project ->
                        navLink(project.name).onClick {
                            state.currentlyVisibleProject.value = project
                        }
                    }
                }
            }

            div(className = "row p-4") {
                val dependenciesChartContainer = Div(className = "card")
                val languageRatioChartContainer = Div(className = "card")
                div(className = "col") {
                    add(dependenciesChartContainer)
                }
                div(className = "col") {
                    add(languageRatioChartContainer)
                }
                AppScope.launch {
                    state.currentlyVisibleProject.combine(state.statistics) { project, statistics ->
                        val records = statistics[project]

                        if (records != null) {
                            dependenciesChartContainer.removeAll()
                            dependenciesChartContainer.add(DependenciesChart(records))

                            languageRatioChartContainer.removeAll()
                            languageRatioChartContainer.add(LanguageRatioChart(records))
                        }

                    }.collect()
                }
            }

            Project.values().forEach { project ->
                CoroutineScope(Dispatchers.Default + Job()).launch {
                    service.getStatisticsFor(project).also { records ->
                        state.statistics.value = state.statistics.value.toMutableMap().apply { set(project, records) }
                    }
                }
            }


        }
    }
}

fun main() {
    startApplication(
            ::App,
            module.hot,
            BootstrapModule,
            FontAwesomeModule,
            ChartModule,
            CoreModule
    )
}
