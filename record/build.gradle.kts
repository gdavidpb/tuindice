plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.record"
		compileSdk = 36
		minSdk = 24

		androidResources {
			enable = true
		}

		withHostTest {}
		withDeviceTest {
			instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
				implementation(libs.koin.core.viewmodel)
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.components.resources)
				implementation(libs.kotlinx.datetime)
				implementation(libs.material.icons.extended)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(project(":testkit"))
			}
		}

		val androidHostTest by getting {
			dependencies {
				implementation(libs.bundles.testing)
				implementation(libs.ktor.client.cio)
			}
		}

		val androidDeviceTest by getting {
			dependencies {
				implementation(project.dependencies.platform(libs.compose.bom))
				implementation(libs.bundles.testing.android)
				implementation(libs.test.ext.junit)
				implementation(libs.compose.ui.test.junit4)
			}
		}
	}
}
