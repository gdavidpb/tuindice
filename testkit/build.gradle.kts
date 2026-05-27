plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.testkit"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isIncludeAndroidResources = true
			isReturnDefaultValues = true
		}
	}

	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(project(":base"))
				api(kotlin("test"))
				api(libs.koin.core)
				api(libs.kotlinx.coroutines.test)
				api(libs.turbine)
				api(libs.jetbrains.compose.ui.test)
				implementation(libs.ktor.client.mock)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}
}
