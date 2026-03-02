plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.summary"
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
				implementation(libs.filekit.core)
				implementation(libs.filekit.dialogs.compose)
				implementation(libs.coil3.compose)
				implementation(libs.coil3.network.ktor3)
				implementation(libs.datastore.preferences)
				implementation(libs.navigation.compose)
				implementation(libs.koin.compose)
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

		val androidMain by getting {
			dependencies {
				implementation(libs.compose.activity)
				implementation(libs.core.ktx)
			}
		}
	}
}
