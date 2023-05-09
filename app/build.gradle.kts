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
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("commons-codec:commons-codec:1.13")
}
