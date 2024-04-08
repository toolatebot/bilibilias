@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    kotlin("kapt")
}
apply {
    from("../config.gradle")
}

ksp {
    arg("ModuleName", project.name)
}

android {
    namespace = "com.imcys.bilibilias.common"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    dataBinding {
        enable = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
kotlin {
    jvmToolchain(17)
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

dependencies {

    // 深拷贝
    api(libs.deeprecopy.core)
    implementation(project(":core:network"))
    ksp(libs.deeprecopy.compiler)

    // hilt库，实现控制反转
    api(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 文件下载
    api(libs.okdownload)

    // 提供kotlin extension，可以不引入
    api(libs.ktx)

    /**
     * SmoothRefreshLayout支持
     */
    api(libs.srl.core)
    api(libs.srl.ext.material)
    api(libs.srl.ext.classics)

    /**
     * MMKV 储存框架
     */
    api(libs.mmkv)

    /**
     * 伸缩布局
     */
    api(libs.flexbox)

    /**
     * jxl库
     * 直接生成excel文件时采用
     */
    api(libs.jxl)

    /**
     * 网络图片加载库
     * 专为compose打造
     */
    api(libs.coil.compose)

    /**
     * rv框架
     * 实现RV的动画效果
     */
    api(libs.brv)

    /**
     * 底部对话框库
     * 为AS专门打造适配的对话框库
     */
    api(libs.asBottomDialog)

    // 协程
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.serialization.cbor)

    /**
     * RxFFmpeg
     * 支持视频合并等操作
     */
    api(libs.rxFFmpeg)

    // 百度统计
    api(libs.mtj.sdk.circle)

    // 微软分发
    api(libs.appcenter.distribute)
    // 微软统计
    api(libs.appcenter.analytics)
    api(libs.appcenter.crashes)

    /**
     * room
     * 本地化数据库
     */
    api(libs.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    /**
     * DanmakuFlameMaster
     * 烈焰弹幕使
     */
    api(libs.dfm)

    // 饺子播放器
    api(libs.jiaozivideoplayer)

    // lottie动画库
    api(libs.lottie)

    api(libs.banner)

    api(libs.glide)

    /**
     * 沉浸式布局库
     */
    api(libs.uiTimateBarX)

    debugImplementation(libs.monitor)
    releaseImplementation(libs.monitor.no.op)
    implementation(libs.okhttp)
    implementation(libs.okhttp.brotli)

    /**
     * ktor全局支持
     */
    api(libs.ktor.client.okhttp)
    api(libs.napier)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)

    api(libs.constraintlayout)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.preference.ktx)

    api(libs.androidx.activity.compose)

    api(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
