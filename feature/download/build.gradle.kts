﻿@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.bilibili.android.feature)
    alias(libs.plugins.bilibili.android.compose)
}

android {
    namespace = "com.bilias.feature.download"
}

dependencies {
    implementation(project(":core:network"))
}
