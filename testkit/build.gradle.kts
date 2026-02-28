plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.testkit"
		compileSdk = 36
		minSdk = 24
	}

	iosX64()
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(project(":base"))
				api(kotlin("test"))
				api(libs.kotlinx.coroutines.test)
				api(libs.turbine)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}
}
