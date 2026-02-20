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
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
