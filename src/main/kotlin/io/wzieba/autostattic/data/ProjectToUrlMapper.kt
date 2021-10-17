package io.wzieba.autostattic.data

import io.wzieba.autostattic.domain.Project

val Project.repositoryName: String
    get() = when (this) {
        Project.WOOCOMMERCE -> "woocommerce-android.csv"
        Project.WORDPRESS -> "WordPress-Android.csv"
        Project.FLUXC -> "WordPress-FluxC-Android.csv"
        Project.SIMPLENOTE -> "simplenote-android.csv"
        Project.WCSTOREAPP -> "WCStoreApp.csv"
    }
