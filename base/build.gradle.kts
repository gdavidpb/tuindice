plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.base"
		compileSdk = 36
		minSdk = 24

		androidResources {
			enable = true
		}
	}
	iosX64()
	iosArm64()
	iosSimulatorArm64()

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(libs.compose.runtime)
				api(libs.bundles.compose.mpp.core)
				api(libs.navigation.compose)
				api(libs.kotlinx.coroutines.core)
				api(libs.ktor.client.core)
				api(libs.lifecycle.runtime.compose)

				implementation(libs.components.resources)
				implementation(libs.kotlinx.datetime)
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.datastore.preferences)
				implementation(libs.koin.core)
				implementation(libs.compottie)
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
				/* Compose */
				implementation(project.dependencies.platform(libs.compose.bom))
				implementation(libs.bundles.compose)

				/* AndroidX */
				implementation(libs.bundles.androidx)

				/* Koin */
				implementation(libs.koin.compose)
			}
		}

		val iosMain by getting
	}
}
