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
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isIncludeAndroidResources = true
			isReturnDefaultValues = true
		}

		androidResources {
			enable = true
		}
		withDeviceTest {
			instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		}
	}
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":academiccore"))
				implementation(project(":base"))
				implementation(project(":persistence"))
				implementation(libs.navigation.compose)
				implementation(libs.jetbrains.compose.backhandler)
				implementation(libs.koin.compose)
				implementation(libs.koin.core.viewmodel)
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.components.resources)
				implementation(libs.kotlinx.datetime)
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.material.icons.extended)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(project(":testkit"))
				implementation(libs.ktor.client.mock)
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
