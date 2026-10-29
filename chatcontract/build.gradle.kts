plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.kotlin.serialization)
}

// Chat contract shared by the app and the backend (tuindice-api consumes the jvm artifact): events, message parts,
// components, intents and screen context of the academic assistant. Pure Kotlin, no Compose and no IO.
kotlin {
	explicitApi()

	android {
		namespace = "com.gdavidpb.tuindice.chatcontract"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isIncludeAndroidResources = true
			isReturnDefaultValues = true
		}
	}
	iosArm64()
	iosSimulatorArm64()
	jvm()

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(libs.kotlinx.serialization.json)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}
}

apply(from = rootProject.file("gradle/shared-contract-publishing.gradle.kts"))
