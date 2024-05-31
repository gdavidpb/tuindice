plugins {
	id("kotlin-android")
	id("com.android.application")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

android {
	compileSdk = 34

	kotlinOptions {
		jvmTarget = "18"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_18
		targetCompatibility = JavaVersion.VERSION_18
	}

	buildFeatures {
		compose = true
	}

	defaultConfig {
		applicationId = "com.gdavidpb.tuindice"
		minSdk = 23
		targetSdk = 34
		versionCode = 35
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