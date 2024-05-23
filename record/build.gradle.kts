plugins {
    id("com.android.library")
    id("kotlin-android")
    id(libs.plugins.kotlin.serialization.get().pluginId) version libs.versions.kotlin.serialization.get()
}

android {
    namespace = "com.gdavidpb.tuindice.record"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(project(":base"))
    implementation(project(":persistence"))
}