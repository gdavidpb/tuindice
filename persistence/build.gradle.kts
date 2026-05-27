plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")

	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.persistence"
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
				implementation(project(":base"))
				implementation(libs.koin.core)
				implementation(libs.kermit)
				implementation(libs.kotlinx.datetime)
				api(libs.room.runtime)
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.store5)
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
				implementation(libs.koin.android)
				implementation(libs.room.ktx)
			}
		}

		val iosMain by getting {
			dependencies {
				implementation(libs.sqlite.bundled)
			}
		}
	}
}

dependencies {
	add("kspAndroid", libs.room.compiler)
	add("kspIosArm64", libs.room.compiler)
	add("kspIosSimulatorArm64", libs.room.compiler)
}
