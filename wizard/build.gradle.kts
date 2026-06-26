plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.wizard"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isIncludeAndroidResources = true
			isReturnDefaultValues = true
		}

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
				implementation(project(":summary"))
				implementation(project(":record"))
				implementation(project(":evaluations"))
				implementation(project(":pensum"))
				implementation(project(":about"))
				implementation(project(":subjects"))
				implementation(libs.koin.core.viewmodel)
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.components.resources)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(project(":testkit"))
			}
		}
	}
}
