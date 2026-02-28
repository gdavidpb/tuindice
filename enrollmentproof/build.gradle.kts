plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.enrollmentproof"
		compileSdk = 36
		minSdk = 24

		androidResources {
			enable = true
		}
	}
	iosX64()
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":persistence"))
				implementation(libs.navigation.compose)
				implementation(libs.koin.compose)
				implementation(libs.components.resources)
				implementation(libs.compottie)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}

		val androidMain by getting {
			dependencies {
				implementation(project(":base"))
			}
		}
	}
}
