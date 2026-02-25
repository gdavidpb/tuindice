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
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "maincore"
			isStatic = true
		}
	}

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":persistence"))
				implementation(project(":login"))
				implementation(project(":about"))
				implementation(project(":summary"))
				implementation(project(":record"))
				implementation(project(":evaluations"))
				implementation(project(":enrollmentproof"))
				implementation(libs.koin.compose.viewmodel)
				implementation(libs.navigation.compose)
				implementation(libs.bundles.ktor)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(libs.ktor.client.mock)
				implementation(libs.ktor.client.content.negotiation)
				implementation(libs.ktor.serialization.kotlinx.json)
			}
		}

		val iosMain by creating {
			dependsOn(commonMain)
		}
		val iosArm64Main by getting {
			dependsOn(iosMain)
		}
		val iosX64Main by getting {
			dependsOn(iosMain)
		}
		val iosSimulatorArm64Main by getting {
			dependsOn(iosMain)
		}
		val iosSimulatorMain by creating {
			dependsOn(iosMain)
			iosX64Main.dependsOn(this)
			iosSimulatorArm64Main.dependsOn(this)
		}

		val androidMain by getting
	}
}
