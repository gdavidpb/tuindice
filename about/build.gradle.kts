plugins {
    id("com.android.library")
    id("kotlin-android")

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.gdavidpb.tuindice.about"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
    }

    kotlinOptions {
        jvmTarget = "18"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_18)
        targetCompatibility(JavaVersion.VERSION_18)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "URL_X", "\"https://x.com/TuIndice/\"")
            buildConfigField("String", "URL_GITHUB", "\"https://github.com/gdavidpb/tuindice/\"")
            buildConfigField("String", "URL_KOTLIN", "\"https://kotlinlang.org/\"")
            buildConfigField("String", "URL_COMPOSE", "\"https://developer.android.com/jetpack/compose/\"")
            buildConfigField("String", "URL_FIREBASE", "\"https://firebase.com/\"")
            buildConfigField("String", "URL_KOIN", "\"https://insert-koin.io/\"")
            buildConfigField("String", "URL_KTOR", "\"https://ktor.io/\"")
            buildConfigField("String", "URL_DST", "\"https://www.dst.usb.ve/inicio/\"")
            buildConfigField("String", "URL_CREATIVE_COMMONS", "\"https://creativecommons.org/licenses/by-nc/4.0/\"")
        }

        getByName("release") {
            buildConfigField("String", "URL_X", "\"https://x.com/TuIndice/\"")
            buildConfigField("String", "URL_GITHUB", "\"https://github.com/gdavidpb/tuindice/\"")
            buildConfigField("String", "URL_KOTLIN", "\"https://kotlinlang.org/\"")
            buildConfigField("String", "URL_COMPOSE", "\"https://developer.android.com/jetpack/compose/\"")
            buildConfigField("String", "URL_FIREBASE", "\"https://firebase.com/\"")
            buildConfigField("String", "URL_KOIN", "\"https://insert-koin.io/\"")
            buildConfigField("String", "URL_KTOR", "\"https://ktor.io/\"")
            buildConfigField("String", "URL_DST", "\"https://www.dst.usb.ve/inicio/\"")
            buildConfigField("String", "URL_CREATIVE_COMMONS", "\"https://creativecommons.org/licenses/by-nc/4.0/\"")
        }
    }
}

dependencies {
    implementation(project(":base"))
}