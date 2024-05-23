buildscript {
	repositories {
		google()
		mavenCentral()
	}

	dependencies {
		classpath(libs.google.services)
		classpath(libs.android.gradle.plugin)
		classpath(libs.firebase.crashlytics.gradle)
		classpath(libs.kotlin.gradle.plugin)
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}

tasks.register("clean", Delete::class) {
	delete(layout.buildDirectory)
}