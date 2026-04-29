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
				implementation(project(":academiccore"))
				implementation(project(":base"))
				implementation(project(":summary"))
				implementation(project(":record"))
				implementation(project(":evaluations"))
				implementation(project(":subjects"))
				implementation(project(":about"))
				implementation(project(":enrollmentproof"))
				implementation(libs.navigation.compose)
				implementation(libs.koin.compose)
				implementation(libs.koin.core.viewmodel)
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.components.resources)
				implementation(libs.material.icons.extended)
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
