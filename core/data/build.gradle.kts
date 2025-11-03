plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.travelfoodie.core.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load API keys from .env file
        val envFile = project.rootProject.file(".env")
        if (envFile.exists()) {
            envFile.readLines().forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2 && !line.startsWith("#")) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    buildConfigField("String", key, "\"$value\"")
                }
            }
        } else {
            // Default values if .env doesn't exist
            buildConfigField("String", "OPENAI_API_KEY", "\"\"")
            buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.codegen)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    // Core
    implementation(libs.core.ktx)
}
