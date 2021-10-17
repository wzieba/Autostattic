package io.wzieba.autostattic.presentation

import io.wzieba.autostattic.domain.Project
import io.wzieba.autostattic.domain.Record
import kotlinx.coroutines.flow.MutableStateFlow

class ViewState {

    val currentlyVisibleProject = MutableStateFlow(Project.WOOCOMMERCE)
    val statistics = MutableStateFlow(emptyMap<Project, List<Record>>())
}