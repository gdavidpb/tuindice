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
				api(compose.components.resources)
				api(libs.navigation.compose)
				api(libs.kotlinx.datetime)
				api(libs.kotlinx.serialization.json)
				api(libs.kotlinx.coroutines.core)
				api(libs.ktor.client.core)
				api(libs.datastore.preferences)
				api(libs.koin.core)
				api(libs.lifecycle.runtime.compose)
				api(libs.compottie)
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

				/* Firebase */
				api(project.dependencies.platform(libs.firebase.bom))
				api(libs.bundles.firebase)

				/* Ktor */
				api(libs.bundles.ktor)
				api(libs.ktor.client.okhttp)

				/* Coil */
				api(libs.bundles.coil)

			}
		}

		val iosMain by getting {
			dependencies {
				api(libs.ktor.client.darwin)
			}
		}
	}
}
