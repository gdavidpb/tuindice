plugins {
	id("kotlin-android")
	id("com.android.library")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.gdavidpb.tuindice.base"
	compileSdk = 36

	defaultConfig {
		minSdk = 23
	}

	compileOptions {
		sourceCompatibility(JavaVersion.VERSION_21)
		targetCompatibility(JavaVersion.VERSION_21)
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	buildTypes {
		debug {
			buildConfigField("String", "MASTER_KEY_ALIAS", "\"tuindice_key\"")

			buildConfigField("String", "URL_API", "\"http://10.0.2.2:8080/\"")
			buildConfigField("String", "URL_PRIVACY_POLICY", "\"https://tuindice.app/privacy_policy.html\"")
			buildConfigField("String", "URL_TERMS_AND_CONDITIONS", "\"https://tuindice.app/terms_and_conditions.html\"")
		}
		release {
			buildConfigField("String", "MASTER_KEY_ALIAS", "\"tuindice_key\"")

			buildConfigField("String", "URL_API", "\"https://api.tuindice.app/\"")
			buildConfigField("String", "URL_PRIVACY_POLICY", "\"https://tuindice.app/privacy_policy.html\"")
			buildConfigField("String", "URL_TERMS_AND_CONDITIONS", "\"https://tuindice.app/terms_and_conditions.html\"")
		}
	}
}

dependencies {
	/* Compose */
	api(platform(libs.compose.bom))
	api(libs.bundles.compose)

	/* AndroidX */
	api(libs.bundles.androidx)
	api(libs.bundles.navigation)
	api(libs.bundles.architecture)
	api(libs.bundles.lifecycle)
	api(libs.bundles.coroutines)

	/* Kotlin */
	api(libs.bundles.kotlin)

	/* Koin */
	api(libs.bundles.koin)

	/* Firebase */
	api(platform(libs.firebase.bom))
	api(libs.bundles.firebase)

	/* Google */
	api(libs.bundles.google)

	/* Ktor */
	api(libs.bundles.ktor)

	/* Coil */
	api(libs.bundles.coil)

	/* Lottie */
	api(libs.lottie.compose)

	/* Store */
	api(libs.store)
}