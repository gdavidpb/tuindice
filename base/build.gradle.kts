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
			}
		}

		val androidMain by getting {
			dependencies {
				/* Compose */
				api(project.dependencies.platform(libs.compose.bom))
				api(libs.bundles.compose)

				/* AndroidX */
				api(libs.bundles.androidx)
				api(libs.bundles.navigation)
				api(libs.bundles.architecture)
				api(libs.bundles.lifecycle)
				api(libs.bundles.coroutines)

				/* Kotlin */
				api(libs.bundles.kotlin)

				/* Koin */
				api(libs.koin.compose)
				api(libs.koin.compose.viewmodel)

				/* Ktor */
				api(libs.bundles.ktor)
				api(libs.ktor.client.okhttp)
			}
		}

		val iosMain by getting {
			dependencies {
				api(libs.ktor.client.darwin)
			}
		}
	}
}
