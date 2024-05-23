plugins {
    id("com.android.library")
    id("kotlin-android")

    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.gdavidpb.tuindice.persistence"
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
}

dependencies {
    implementation(project(":base"))

	/* Room */
    ksp(libs.room.compiler)
    api(libs.bundles.room)
}