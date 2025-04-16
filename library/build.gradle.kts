import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.apollo)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "pw.vodes"
version = "0.0.1-SNAPSHOT1"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlin.serialization.json)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
                api(libs.apollo.runtime)
                implementation(libs.apollo.normalized.cache)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

apollo {
    service("anilist") {
        packageName.set("pw.vodes.anilistkmp.graphql")
        languageVersion.set("1.9")
        generateSourcesDuringGradleSync.set(true)
        generateInputBuilders.set(true)
    }
}

android {
    namespace = "pw.vodes.anilist.kmp.library"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
