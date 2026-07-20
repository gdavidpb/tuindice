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

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(libs.compose.runtime)
				api(libs.bundles.compose.mpp.core)
				api(libs.navigation.compose)
				api(libs.navigation3.runtime)
				implementation(libs.navigation3.ui)
				api(libs.kotlinx.coroutines.core)
				api(libs.ktor.client.core)
				api(libs.lifecycle.runtime.compose)
				api(libs.filekit.core)
				api(libs.kermit)
				api(libs.multiplatform.settings)
				api(libs.ksafe)

				api(libs.components.resources)
				implementation(libs.material.icons.extended)
				implementation(libs.kotlinx.datetime)
				implementation(libs.kotlinx.serialization.json)
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
