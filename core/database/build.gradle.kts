﻿plugins {
    alias(libs.plugins.bilibilias.android.library)
    alias(libs.plugins.bilibilias.android.jacoco)
    alias(libs.plugins.bilibilias.hilt)
    alias(libs.plugins.bilibilias.android.room)
    alias(libs.plugins.bilibilias.sqlLin)
}

android {
    namespace = "com.imcys.bilibilias.core.database"
    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {
    api(projects.core.model)

    implementation(libs.kotlinx.datetime)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
