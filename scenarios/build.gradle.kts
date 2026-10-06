plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.scenarios"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isReturnDefaultValues = true
		}
	}

	// Declared so CI derives the per-module iOS tasks; the catalog has no framework binary.
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":scenariokit"))
				implementation(project(":about"))
				implementation(project(":auth"))
				implementation(project(":base"))
				implementation(project(":enrollmentproof"))
				implementation(project(":evaluations"))
				implementation(project(":maincore"))
				implementation(project(":pensum"))
				implementation(project(":record"))
				implementation(project(":subjects"))
				implementation(project(":summary"))
				implementation(project(":wizard"))
			}
		}

		val androidHostTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(libs.kotlinx.serialization.json)
			}
		}
	}
}

// The host tests read repo files outside this module and write the generated catalog artifacts,
// so both ends are declared: a changed mock or tag file reruns them, and a cache hit restores the output.
tasks.withType<Test>().configureEach {
	if (name == "testAndroidHostTest") {
		inputs.dir(rootProject.file("mocks")).withPropertyName("mocks")
		inputs.files(
			rootProject.fileTree(rootDir) {
				include("*/src/commonMain/**/*UiTags.kt")
			}
		).withPropertyName("uiTags")
		outputs.dir(layout.buildDirectory.dir("e2e/catalog"))
	}
}
