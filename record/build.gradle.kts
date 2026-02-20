import com.android.build.api.dsl.LibraryExtension

plugins {
	id("com.android.library")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

extensions.configure<LibraryExtension> {
	namespace = "com.gdavidpb.tuindice.record"
	compileSdk = 36

	defaultConfig {
		minSdk = 24
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	compileOptions {
		sourceCompatibility(JavaVersion.VERSION_21)
		targetCompatibility(JavaVersion.VERSION_21)
	}

	buildFeatures {
		compose = true
		resValues = false
	}
}

dependencies {
	implementation(project(":base"))
	implementation(project(":persistence"))

	testImplementation(libs.bundles.testing)
	androidTestImplementation(platform(libs.compose.bom))
	androidTestImplementation(libs.bundles.testing.android)
	androidTestImplementation(libs.test.ext.junit)
	androidTestImplementation(libs.compose.ui.test.junit4)
	debugImplementation(libs.compose.ui.test.manifest)
}