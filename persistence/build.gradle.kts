plugins {
    id("com.android.library")
    id("kotlin-android")
    id(libs.plugins.ksp.get().pluginId) version libs.versions.ksp.get()
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(project(":base"))

	/* Room */
    ksp(libs.room.compiler)
    api(libs.bundles.room)
}