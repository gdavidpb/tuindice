plugins {
	id("com.android.test")
}

android {
	namespace = "com.gdavidpb.tuindice.scenariorunner"
	compileSdk = 37

	defaultConfig {
		minSdk = 24
		targetSdk = 36
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	targetProjectPath = ":app"
	experimentalProperties["android.experimental.self-instrumenting"] = true

	// The runners read the versioned catalog; nothing here builds or refreshes it.
	sourceSets["main"].assets.srcDir(rootProject.layout.projectDirectory.dir("e2e/catalog"))
}

androidComponents {
	beforeVariants { variant -> variant.enable = variant.buildType == "debug" }
}

dependencies {
	implementation(project(":scenariokit"))
	implementation(libs.uiautomator)
	implementation(libs.test.runner)
	implementation(libs.test.ext.junit)
	implementation(libs.junit)
}
