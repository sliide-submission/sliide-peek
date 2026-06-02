import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val generatedBuildSecretsDir = layout.buildDirectory.dir("generated/build-secrets/commonMain/kotlin")

val generateBuildSecrets by tasks.registering {
    notCompatibleWithConfigurationCache("Reads local secret files at execution time")
    val outputDir = generatedBuildSecretsDir
    val tokenInput = findGorestToken()
    inputs.property("gorestTokenHash", tokenInput.hashCode())
    outputs.dir(outputDir)

    doLast {
        val token = tokenInput
        val packageDir = outputDir.get().dir("com/sliide/useractivity/config").asFile
        packageDir.mkdirs()
        packageDir.resolve("BuildSecrets.kt").writeText(
            """
            package com.sliide.useractivity.config

            internal object BuildSecrets {
                const val gorestToken: String = ${token.toKotlinStringLiteral()}
            }
            """.trimIndent() + "\n",
        )
    }
}

fun findGorestToken(): String =
    System.getenv("GOREST_TOKEN")?.takeIf { it.isNotBlank() }
        ?: propertiesToken(layout.projectDirectory.file("../local.properties").asFile)
        ?: envFileToken(layout.projectDirectory.file("../.env").asFile)
        ?: ""

fun propertiesToken(file: File): String? {
    if (!file.isFile) return null
    return file.inputStream().use { input ->
        Properties().apply { load(input) }
    }.getProperty("gorest.token")?.takeIf { it.isNotBlank() }
}

fun envFileToken(file: File): String? {
    if (!file.isFile) return null
    return file.readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim().trim('"', '\'')
            if (key == "GOREST_TOKEN" || key == "gorest.token") value.takeIf { it.isNotBlank() } else null
        }
        .firstOrNull()
}

fun String.toKotlinStringLiteral(): String = buildString {
    append('"')
    this@toKotlinStringLiteral.forEach { char ->
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "com.sliide.useractivity.shared")
        }
    }
    
    androidLibrary {
       namespace = "com.sliide.useractivity.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedBuildSecretsDir)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

sqldelight {
    databases {
        create("SliidePeekDatabase") {
            packageName.set("com.sliide.useractivity.data.local")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.sliide.useractivity.resources"
}

tasks.matching { it.name.startsWith("compile") || it.name.contains("Kotlin") }.configureEach {
    dependsOn(generateBuildSecrets)
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
