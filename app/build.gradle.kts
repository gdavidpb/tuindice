import com.android.build.api.dsl.ApplicationExtension

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
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
		versionCode = 36
		versionName = "5.8"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
	implementation(libs.bundles.google)
	implementation(platform(libs.firebase.bom))
	implementation(libs.bundles.firebase)
	implementation(libs.koin.android)
	testImplementation(libs.bundles.testing)
	androidTestImplementation(libs.bundles.testing.android)
	androidTestImplementation(libs.test.ext.junit)

	implementation(project(":base"))
	implementation(project(":maincore"))
	implementation(project(":persistence"))
	implementation(project(":login"))
	implementation(project(":about"))
	implementation(project(":summary"))
	implementation(project(":record"))
	implementation(project(":enrollmentproof"))
	implementation(project(":evaluations"))
}
