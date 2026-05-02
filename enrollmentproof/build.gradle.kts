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
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":persistence"))
				implementation(libs.navigation.compose)
				implementation(libs.koin.compose)
				implementation(libs.koin.core.viewmodel)
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.components.resources)
				implementation(libs.filekit.core)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(project(":testkit"))
				implementation(libs.ktor.client.mock)
			}
		}
	}
}
