plugins {
	id("com.android.library")
	id("kotlin-android")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.gdavidpb.tuindice.enrollmentproof"
	compileSdk = 36

	defaultConfig {
		minSdk = 24
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
	implementation(project(":persistence"))
}
