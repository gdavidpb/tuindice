plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.scenariokit"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isReturnDefaultValues = true
		}
	}

	listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
		target.binaries.framework {
			baseName = "ScenarioKit"
			isStatic = true
			binaryOption("bundleId", "com.gdavidpb.tuindice.scenariokit")
		}
	}

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlinx.serialization.json)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}
}
