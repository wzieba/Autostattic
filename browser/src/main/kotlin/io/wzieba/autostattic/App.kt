package io.wzieba.autostattic

import io.kvision.*
import io.kvision.html.*
import io.kvision.navbar.*
import io.kvision.navbar.nav
import io.kvision.panel.*
import io.wzieba.autostattic.data.InnerFilesService
import io.wzieba.autostattic.domain.Project
import io.wzieba.autostattic.presentation.CompilerWarningsChart
import io.wzieba.autostattic.presentation.DependenciesChart
import io.wzieba.autostattic.presentation.LanguageRatioChart
import io.wzieba.autostattic.presentation.ViewState
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

val AppScope = CoroutineScope(window.asCoroutineDispatcher())
val service = InnerFilesService()
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

            div(){
                val dependenciesChartContainer = Div(className = "card")
                val languageRatioChartContainer = Div(className = "card")
                val compilerWarningsChartContainer = Div(className = "card")

                div(className = "row p-4") {
                    div(className = "col-6") {
                        add(dependenciesChartContainer)
                    }
                    div(className = "col-6") {
                        add(languageRatioChartContainer)
                    }

                }
                div(className = "row p-4"){
                    div(className = "col-6"){
                        add(compilerWarningsChartContainer)
                    }
                }
                AppScope.launch {
                    state.currentlyVisibleProject.combine(state.statistics) { project, statistics ->
                        val records = statistics[project]

                        if (records != null) {
                            dependenciesChartContainer.removeAll()
                            dependenciesChartContainer.add(DependenciesChart(records))

                            languageRatioChartContainer.removeAll()
                            languageRatioChartContainer.add(LanguageRatioChart(records))

                            compilerWarningsChartContainer.removeAll()
                            compilerWarningsChartContainer.add(CompilerWarningsChart(records))
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
