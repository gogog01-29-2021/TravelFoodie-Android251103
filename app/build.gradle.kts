plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.travelfoodie"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.travelfoodie"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Manifest placeholders for API keys
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY", "")
        manifestPlaceholders["KAKAO_API_KEY"] = properties.getProperty("KAKAO_API_KEY", "")
        
        buildConfigField("String", "KAKAO_API_KEY", "\"${properties.getProperty("KAKAO_API_KEY", "")}\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${properties.getProperty("NAVER_CLIENT_ID", "")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${properties.getProperty("NAVER_CLIENT_SECRET", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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
        buildConfig = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:sync"))
    implementation(project(":core:sensors"))
    
    // Feature modules
    implementation(project(":feature:trip"))
    implementation(project(":feature:attraction"))
    implementation(project(":feature:restaurant"))
    implementation(project(":feature:voice"))
    implementation(project(":feature:widget"))
    implementation(project(":feature:board"))

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)

    // OAuth / Social Login
    implementation(libs.play.services.auth)
    implementation(libs.kakao.user)
    implementation(libs.kakao.share)
    implementation(libs.naver.login)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.codegen)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Lifecycle
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // Location & Maps
    implementation(libs.location)
    implementation(libs.maps)
    implementation(libs.places)

    // UI
    implementation(libs.material)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lottie)
    implementation(libs.paging)
    
    // WorkManager
    implementation(libs.work.runtime)
    
    // ML Kit
    implementation(libs.mlkit.text)
}
