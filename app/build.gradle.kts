plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun getCommitCount(): Int {
    val stdout = org.apache.commons.io.output.ByteArrayOutputStream()
    project.exec {
        commandLine = "git rev-list --count HEAD".split(" ")
        standardOutput = stdout
    }
    return Integer.parseInt(String(stdout.toByteArray()).trim())
}

android {
    namespace = "it.leddaz.revancedupdater"
    compileSdk = 33

    defaultConfig {
        applicationId = "it.leddaz.revancedupdater"
        minSdk = 23
        targetSdk = 33
        versionCode = getCommitCount()
        versionName = "2.1.3"
        resourceConfigurations += listOf("en", "it")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("commons-codec:commons-codec:1.16.0")
}
