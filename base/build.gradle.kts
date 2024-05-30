plugins {
	id("com.android.library")
	id("kotlin-android")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.gdavidpb.tuindice.base"
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
		debug {
			buildConfigField("String", "APPLICATION_ID", "\"com.gdavidpb.tuindice\"")
			buildConfigField("String", "VERSION_NAME", "\"5.8\"")
			buildConfigField("Integer", "VERSION_CODE", "35")

			buildConfigField("String", "MASTER_KEY_ALIAS", "\"tuindice_key\"")

			buildConfigField("String", "ENDPOINT_TU_INDICE_API", "\"http://10.0.2.2:8080/\"")

			buildConfigField("String", "URL_APP", "\"tu-indice-usb.firebaseapp.com\"")
			buildConfigField("String", "URL_APP_PRIVACY_POLICY", "\"https://tu-indice-usb.firebaseapp.com/privacy_policy.html\"")
			buildConfigField("String", "URL_APP_TERMS_AND_CONDITIONS", "\"https://tu-indice-usb.firebaseapp.com/terms_and_conditions.html\"")
		}
		release {
			buildConfigField("String", "APPLICATION_ID", "\"com.gdavidpb.tuindice\"")
			buildConfigField("String", "VERSION_NAME", "\"5.8\"")
			buildConfigField("Integer", "VERSION_CODE", "35")

			buildConfigField("String", "MASTER_KEY_ALIAS", "\"tuindice_key\"")

			buildConfigField("String", "ENDPOINT_TU_INDICE_API", "\"https://us-central1-tu-indice-usb.cloudfunctions.net/\"")

			buildConfigField("String", "URL_APP", "\"tu-indice-usb.firebaseapp.com\"")
			buildConfigField("String", "URL_APP_PRIVACY_POLICY", "\"https://tu-indice-usb.firebaseapp.com/privacy_policy.html\"")
			buildConfigField("String", "URL_APP_TERMS_AND_CONDITIONS", "\"https://tu-indice-usb.firebaseapp.com/terms_and_conditions.html\"")
		}
	}
}

dependencies {
	debugApi(libs.bundles.debug)

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

	/* Retrofit */
	api(libs.bundles.retrofit)

	/* Ktor */
	api(libs.bundles.ktor)

	/* Coil */
	api(libs.bundles.coil)

	/* Lottie */
	api(libs.lottie.compose)

	/* Store */
	api(libs.store)
}