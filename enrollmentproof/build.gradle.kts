plugins {
	id("com.android.library")
	id("kotlin-android")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.gdavidpb.tuindice.enrollmentproof"
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
	implementation(project(":persistence"))
}
