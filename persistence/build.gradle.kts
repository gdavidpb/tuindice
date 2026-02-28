plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")

	alias(libs.plugins.ksp)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.persistence"
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

	compilerOptions {
		freeCompilerArgs.add("-Xexpect-actual-classes")
	}

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(libs.koin.core)
				api(libs.room.runtime)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
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
	add("kspCommonMainMetadata", libs.room.compiler)
	add("kspAndroid", libs.room.compiler)
	add("kspIosX64", libs.room.compiler)
	add("kspIosArm64", libs.room.compiler)
	add("kspIosSimulatorArm64", libs.room.compiler)
}
