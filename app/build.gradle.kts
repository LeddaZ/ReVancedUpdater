import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun execCommand(command: String): String? {
    val cmd = command.split(" ").toTypedArray()
    val process = ProcessBuilder(*cmd)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    return process.inputStream.bufferedReader().readLine()?.trim()
}

val commitCount by project.extra {
    execCommand("git rev-list --count HEAD")?.toInt()
        ?: throw GradleException("Unable to get number of commits. Make sure git is initialized.")
}

val commitHash by project.extra {
    execCommand("git rev-parse --short HEAD")
        ?: throw GradleException(
            "Unable to get commit hash. Make sure git is initialized."
        )
}

android {
    namespace = "it.leddaz.revancedupdater"
    compileSdk = 36

    defaultConfig {
        applicationId = "it.leddaz.revancedupdater"
        minSdk = 26
        targetSdk = 36
        versionCode = commitCount
        versionName = "4.0.2 ($commitHash)"
    }

    buildTypes {
        getByName("release") {
            // Includes the default ProGuard rules files.
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".dev"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("21")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    @Suppress("UnstableApiUsage")
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.android.volley)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.commons.codec)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.markwon.core)
}
