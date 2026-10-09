plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.scenarios"
		compileSdk = 37
		minSdk = 24

		withHostTest {
			isReturnDefaultValues = true
		}
	}

	// Declared so CI derives the per-module iOS tasks; the catalog has no framework binary.
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":scenariokit"))
				implementation(project(":about"))
				implementation(project(":academiccore"))
				implementation(project(":auth"))
				implementation(project(":base"))
				implementation(project(":enrollmentproof"))
				implementation(project(":evaluations"))
				implementation(project(":maincore"))
				implementation(project(":pensum"))
				implementation(project(":record"))
				implementation(project(":subjects"))
				implementation(project(":summary"))
				implementation(project(":wizard"))
			}
		}

		val androidHostTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(libs.kotlinx.serialization.json)
			}
		}
	}
}

// The host tests read repo files outside this module and write the generated catalog artifacts,
// so both ends are declared: a changed mock, tag, string or host file reruns them, and a cache hit restores
// the output. `TestInputsDeclaredTest` keeps this list in step with what the tests read.
tasks.withType<Test>().configureEach {
	if (name == "testAndroidHostTest") {
		inputs.dir(rootProject.file("mocks")).withPropertyName("mocks")
		inputs.files(
			rootProject.fileTree(rootDir) {
				include("*/src/commonMain/**/*UiTags.kt")
			}
		).withPropertyName("uiTags")
		// The rest of what the tests read from outside this module: the string resources `CopyTest` binds
		// the texts to, the contracts `ActionCoverageTest` lists the actions of, the product sources that
		// `CoachmarkCoverageTest`, `SubjectSearchFixturesTest` and `E2eClockFixtureTest` read, and the debug hosts
		// `LaunchArgsParityTest` compares (the iOS Swift host and the Android and iOS debug code are on no
		// classpath of this module, so a change there would otherwise leave the task up to date), and the sources of the
		// two drivers that `DriverSourcesTest` reads (their refusals and the tags of the Android probes).
		inputs.files(
			rootProject.fileTree(rootDir) {
				include("*/src/commonMain/composeResources/values/strings.xml")
				include("*/src/commonMain/kotlin/**/presentation/contract/*.kt")
				include("wizard/src/commonMain/kotlin/**/mapper/CoachmarkSurface.kt")
				include("evaluations/src/commonMain/kotlin/**/mapper/AcademicWeek.kt")
				include("subjects/src/commonMain/kotlin/**/data/source/DebugSubjectScenarioResolver.kt")
				include("subjects/src/commonMain/kotlin/**/presentation/machine/SubjectSearchMachine.kt")
				include("*/build.gradle.kts")
				include("app/src/debug/**/*.kt")
				include("maincore/src/iosMain/**/*.kt")
				include("iosApp/Sources/TuIndiceHost/**/*.swift")
				include("scenariorunner/src/main/**/*.kt")
				include("iosApp/UITests/*.swift")
				include("iosApp/Config/*.xcconfig")
			}
		).withPropertyName("repoFilesRead")
		outputs.dir(layout.buildDirectory.dir("e2e/catalog"))
	}
}
