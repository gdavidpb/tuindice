import com.android.build.api.dsl.ApplicationExtension

plugins {
	id("com.android.application")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

val releaseKeyAlias = providers.environmentVariable("TU_INDICE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("TU_INDICE_KEY_PASSWORD").orNull
val releaseKeyStorePath = providers.environmentVariable("TU_INDICE_KEY_STORE_PATH").orNull
val releaseKeyStorePassword = providers.environmentVariable("TU_INDICE_KEY_STORE_PASSWORD").orNull
val hasReleaseSigningConfig = listOf(
	releaseKeyAlias,
	releaseKeyPassword,
	releaseKeyStorePath,
	releaseKeyStorePassword
).all { !it.isNullOrBlank() }

extensions.configure<ApplicationExtension> {
	compileSdk = 36

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	buildFeatures {
		compose = true
		buildConfig = true
		resValues = false
	}

	defaultConfig {
		applicationId = "com.gdavidpb.tuindice"
		minSdk = 24
		targetSdk = 36
		versionCode = 38
		versionName = "6.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField("long", "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "375954751920L")
	}

	signingConfigs {
		create("release") {
			if (hasReleaseSigningConfig) {
				keyAlias = releaseKeyAlias
				keyPassword = releaseKeyPassword
				storeFile = file(releaseKeyStorePath!!)
				storePassword = releaseKeyStorePassword
			}
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
			applicationIdSuffix = ".debug"

			buildConfigField("String", "URL_API", "\"http://0.0.0.0:8080/\"")
			buildConfigField(
				"String",
				"URL_PRIVACY_POLICY",
				"\"https://tuindice.app/privacy_policy.html\""
			)
			buildConfigField(
				"String",
				"URL_TERMS_AND_CONDITIONS",
				"\"https://tuindice.app/terms_and_conditions.html\""
			)
		}
		getByName("release") {
			isDebuggable = false
			isMinifyEnabled = true

			buildConfigField("String", "URL_API", "\"https://api.tuindice.app/\"")
			buildConfigField(
				"String",
				"URL_PRIVACY_POLICY",
				"\"https://tuindice.app/privacy_policy.html\""
			)
			buildConfigField(
				"String",
				"URL_TERMS_AND_CONDITIONS",
				"\"https://tuindice.app/terms_and_conditions.html\""
			)

			if (hasReleaseSigningConfig) {
				signingConfig = signingConfigs.getByName("release")
			}

			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	namespace = defaultConfig.applicationId
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose)
	implementation(libs.core.ktx)
	implementation(libs.bundles.ktor)
	implementation(libs.ktor.client.okhttp)
	implementation(libs.bundles.google)
	implementation(platform(libs.firebase.bom))
	implementation(libs.bundles.firebase)
	implementation(libs.koin.android)
	implementation(libs.koin.core)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.play.services)
	implementation(libs.filekit.dialogs)
	testImplementation(libs.bundles.testing)
	testImplementation(libs.ktor.client.mock)
	testImplementation(libs.kotlinx.coroutines.test)
	androidTestImplementation(libs.bundles.testing.android)
	androidTestImplementation(libs.test.ext.junit)

	implementation(project(":base"))
	implementation(project(":maincore"))
	implementation(project(":persistence"))
	implementation(project(":auth"))
	implementation(project(":about"))
	implementation(project(":summary"))
	implementation(project(":record"))
	implementation(project(":enrollmentproof"))
	implementation(project(":evaluations"))
}

tasks.configureEach {
	val isDebugGoogleServicesTask = name.contains("GoogleServices") && name.contains("Debug")
	val isDebugCrashlyticsTask = name.contains("Crashlytics") && name.contains("Debug")

	if (isDebugGoogleServicesTask || isDebugCrashlyticsTask) {
		enabled = false
	}
}
