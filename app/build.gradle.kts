plugins {
	id("kotlin-android")
	id("com.android.application")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

android {
	compileSdk = 36

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	defaultConfig {
		applicationId = "com.gdavidpb.tuindice"
		minSdk = 24
		targetSdk = 36
		versionCode = 36
		versionName = "5.8"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	signingConfigs {
		create("release") {
			keyAlias = System.getenv("TU_INDICE_KEY_ALIAS")
			keyPassword = System.getenv("TU_INDICE_KEY_PASSWORD")
			storeFile = file(System.getenv("TU_INDICE_KEY_STORE_PATH"))
			storePassword = System.getenv("TU_INDICE_KEY_STORE_PASSWORD")
		}
	}

	packaging {
		resources {
			excludes += setOf("DebugProbesKt.bin")
		}
	}

	buildTypes {
		getByName("debug") {
			isDebuggable = true
			isMinifyEnabled = false
		}
		getByName("release") {
			isDebuggable = false
			isMinifyEnabled = true

			signingConfig = signingConfigs.getByName("release")

			proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
		}
	}

	namespace = defaultConfig.applicationId
}

dependencies {
	implementation(libs.security.crypto.ktx)
	testImplementation(libs.bundles.testing)
	androidTestImplementation(libs.bundles.testing.android)

	implementation(project(":base"))
	implementation(project(":persistence"))
	implementation(project(":login"))
	implementation(project(":about"))
	implementation(project(":summary"))
	implementation(project(":record"))
	implementation(project(":enrollmentproof"))
	implementation(project(":evaluations"))
}