plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.maincore"
		compileSdk = 36
		minSdk = 24

		androidResources {
			enable = true
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "maincore"
			isStatic = true
			binaryOption("bundleId", "com.gdavidpb.tuindice.maincore")
		}
	}

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":persistence"))
				implementation(project(":auth"))
				implementation(project(":about"))
				implementation(project(":summary"))
				implementation(project(":record"))
				implementation(project(":evaluations"))
				implementation(project(":enrollmentproof"))
				implementation(project(":subjects"))
				implementation(libs.koin.compose)
				implementation(libs.koin.core)
				implementation(libs.koin.core.viewmodel)
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.components.resources)
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.material.icons.extended)
				implementation(libs.navigation.compose)
				implementation(libs.bundles.ktor)
			}
		}

		val androidMain by getting {
			dependencies {
				implementation(project.dependencies.platform(libs.compose.bom))
				implementation(libs.compose.ui)
			}
		}

		val iosMain by getting {
			dependencies {
				implementation(libs.ktor.client.darwin)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(project(":testkit"))
				implementation(libs.ktor.client.mock)
				implementation(libs.ktor.client.content.negotiation)
				implementation(libs.ktor.serialization.kotlinx.json)
			}
		}
	}
}
