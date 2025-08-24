plugins {
    id("com.android.library")
    id("kotlin-android")

    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.gdavidpb.tuindice.persistence"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
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