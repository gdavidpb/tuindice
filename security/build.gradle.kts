plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.security"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isIncludeAndroidResources = true
			isReturnDefaultValues = true
		}
	}
	iosArm64()
	iosSimulatorArm64()

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.ktor.client.core)
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
