plugins {
	alias(libs.plugins.compose.compiler) apply false
}

buildscript {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}

	dependencies {
		classpath(libs.google.services)
		classpath(libs.android.gradle.plugin)
		classpath(libs.kotlin.gradle.plugin)
		classpath(libs.firebase.crashlytics.gradle)
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}

tasks.register("clean", Delete::class) {
	delete(layout.buildDirectory)
}

tasks.register("verifySharedCompilation") {
	group = "verification"
	description = "Compiles shared modules for Android and iOS simulator targets."

	dependsOn(
		":about:compileAndroidMain",
		":about:compileKotlinIosSimulatorArm64",
		":academiccore:compileAndroidMain",
		":academiccore:compileKotlinIosSimulatorArm64",
		":base:compileAndroidMain",
		":base:compileKotlinIosSimulatorArm64",
		":testkit:compileAndroidMain",
		":testkit:compileKotlinIosSimulatorArm64",
		":enrollmentproof:compileAndroidMain",
		":enrollmentproof:compileKotlinIosSimulatorArm64",
		":evaluations:compileAndroidMain",
		":evaluations:compileKotlinIosSimulatorArm64",
		":auth:compileAndroidMain",
		":auth:compileKotlinIosSimulatorArm64",
		":maincore:compileAndroidMain",
		":maincore:compileKotlinIosSimulatorArm64",
		":persistence:compileAndroidMain",
		":persistence:compileKotlinIosSimulatorArm64",
		":record:compileAndroidMain",
		":record:compileKotlinIosSimulatorArm64",
		":summary:compileAndroidMain",
		":summary:compileKotlinIosSimulatorArm64",
		":subjects:compileAndroidMain",
		":subjects:compileKotlinIosSimulatorArm64",
		":pensum:compileAndroidMain",
		":pensum:compileKotlinIosSimulatorArm64",
		":wizard:compileAndroidMain",
		":wizard:compileKotlinIosSimulatorArm64"
	)
}

tasks.register("verifySharedTests") {
	group = "verification"
	description = "Runs shared tests on iOS simulator arm64 where available in CI/dev machines."

	dependsOn(
		":about:iosSimulatorArm64Test",
		":academiccore:iosSimulatorArm64Test",
		":base:iosSimulatorArm64Test",
		":testkit:iosSimulatorArm64Test",
		":enrollmentproof:iosSimulatorArm64Test",
		":evaluations:iosSimulatorArm64Test",
		":auth:iosSimulatorArm64Test",
		":persistence:iosSimulatorArm64Test",
		":record:iosSimulatorArm64Test",
		":summary:iosSimulatorArm64Test",
		":subjects:iosSimulatorArm64Test",
		":pensum:iosSimulatorArm64Test",
		":wizard:iosSimulatorArm64Test",
		":maincore:iosSimulatorArm64Test"
	)
}

private val commonUiModules = listOf(
	"about",
	"base",
	"enrollmentproof",
	"evaluations",
	"auth",
	"maincore",
	"pensum",
	"record",
	"subjects",
	"summary",
	"testkit",
	"wizard"
)

tasks.register<Exec>("refreshCommonUiMatrix") {
	group = "verification"
	description = "Regenerates docs/testing/common-ui-matrix.md from commonMain composable inventory."
	commandLine("bash", "${rootDir}/scripts/generate-common-ui-matrix.sh")
}

tasks.register<Exec>("verifyCommonUiTestDensity") {
	group = "verification"
	description = "Fails when a UiTest is below the configured when_ threshold for its module."
	commandLine("bash", "${rootDir}/scripts/verify-common-ui-test-density.sh", "2")
}

tasks.register("verifyCommonUiCompilationGate") {
	group = "verification"
	description = "Compiles common UI tests for iOS simulator arm64 in all shared modules."
	dependsOn(
		commonUiModules.map { moduleName ->
			":$moduleName:compileTestKotlinIosSimulatorArm64"
		}
	)
}

tasks.register("verifyCommonUiTests") {
	group = "verification"
	description = "Runs common UI tests on iOS simulator arm64 for all shared modules."
	dependsOn(
		commonUiModules.map { moduleName ->
			":$moduleName:iosSimulatorArm64Test"
		}
	)
}

tasks.register("verifyCommonUiGate") {
	group = "verification"
	description = "Runs the complete common UI validation gate (matrix + compilation + execution)."
	dependsOn(
		"refreshCommonUiMatrix",
		"verifyCommonUiTestDensity",
		"verifyCommonUiCompilationGate",
		"verifyCommonUiTests"
	)
}

tasks.register<Exec>("verifyE2eContract") {
	group = "verification"
	description = "Validates the local E2E flow catalog and critical selector coverage."
	commandLine("bash", "${rootDir}/testkit/e2e/validate-e2e-contract.sh")
}

tasks.register<Exec>("verifyAppVersionSync") {
	group = "verification"
	description = "Validates Android and iOS app versions against the shared app version properties."
	commandLine("bash", "${rootDir}/.github/scripts/validate-app-version.sh")
}

tasks.register<Exec>("e2eMaestroAndroid") {
	group = "verification"
	description = "Builds the Android debug app and runs the local Maestro E2E certification suite against WireMock."
	commandLine("bash", "${rootDir}/e2e/scripts/run-maestro-android.sh")
}

tasks.register<Exec>("e2eMaestroIos") {
	group = "verification"
	description = "Builds the iOS debug host and runs the local Maestro E2E certification suite against WireMock."
	commandLine("bash", "${rootDir}/e2e/scripts/run-maestro-ios.sh")
}

tasks.register<Exec>("e2eMaestroLocal") {
	group = "verification"
	description = "Runs the local Maestro E2E certification suite on every locally available platform."
	commandLine("bash", "${rootDir}/e2e/scripts/run-maestro-local.sh")
}

tasks.register<Exec>("e2eMaestroEvidenceAndroid") {
	group = "verification"
	description = "Runs Android Maestro E2E and writes commit-bound evidence logs and metadata."
	commandLine("bash", "${rootDir}/e2e/scripts/run-maestro-evidence.sh", "android")
}

tasks.register<Exec>("e2eMaestroEvidenceIos") {
	group = "verification"
	description = "Runs iOS Maestro E2E and writes commit-bound evidence logs and metadata."
	commandLine("bash", "${rootDir}/e2e/scripts/run-maestro-evidence.sh", "ios")
}

tasks.register<Exec>("e2eMaestroEvidenceLocal") {
	group = "verification"
	description = "Runs local Maestro E2E evidence on every locally available platform."
	commandLine("bash", "${rootDir}/e2e/scripts/run-maestro-evidence-local.sh")
}

tasks.register<Exec>("e2ePlatformAndroid") {
	group = "verification"
	description = "Runs Android-only E2E edge suites when registered."
	commandLine("bash", "${rootDir}/e2e/scripts/run-platform-android.sh")
}

tasks.register<Exec>("e2ePlatformIos") {
	group = "verification"
	description = "Runs iOS-only E2E edge suites when registered."
	commandLine("bash", "${rootDir}/e2e/scripts/run-platform-ios.sh")
}

tasks.register<Exec>("verifyIosHostTypecheck") {
	group = "verification"
	description = "Type-checks iOS host Swift sources against linked maincore.framework."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)

	if (isMacHost) {
		dependsOn(":maincore:linkDebugFrameworkIosSimulatorArm64")
	}

	onlyIf {
		isMacHost
	}

	environment("SKIP_FRAMEWORK_BUILD", "1")
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-typecheck-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostBuildDebug") {
	group = "verification"
	description = "Builds iOS host app in Debug configuration (simulator)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"

	onlyIf {
		isMacHost && runHostBuild
	}

	environment("CONFIGURATION", "Debug")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-debug")
	if (System.getenv("TUINDICE_IOS_HOST_REQUIRE_SIMULATOR") == "1") {
		environment("REQUIRE_SIMULATOR", "1")
	}
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-build-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostBuildRelease") {
	group = "verification"
	description = "Builds iOS host app in Release configuration (simulator)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"

	onlyIf {
		isMacHost && runHostBuild
	}

	environment("CONFIGURATION", "Release")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-release")
	if (System.getenv("TUINDICE_IOS_HOST_REQUIRE_SIMULATOR") == "1") {
		environment("REQUIRE_SIMULATOR", "1")
	}
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-build-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostBuildDeviceRelease") {
	group = "verification"
	description = "Builds iOS host app in Release configuration (physical device)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"
	val runHostDeviceE2E = System.getenv("TUINDICE_IOS_HOST_E2E") == "1"

	onlyIf {
		isMacHost && runHostBuild && runHostDeviceE2E
	}

	environment("CONFIGURATION", "Release")
	environment("IOS_PLATFORM", "device")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-device-release")
	environment(
		"CODE_SIGNING_ALLOWED",
		System.getenv("TUINDICE_IOS_HOST_DEVICE_CODE_SIGNING_ALLOWED") ?: "YES"
	)
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-build-ios-host.sh")
}

tasks.register<Exec>("reportWorktreeHealth") {
	group = "help"
	description = "Generates a worktree health report with suggested atomic commit batches."
	val reportScript = file("${rootDir}/scripts/report-worktree-health.sh")

	onlyIf {
		reportScript.exists()
	}

	doFirst {
		check(reportScript.exists()) {
			"Missing worktree report script: ${reportScript.absolutePath}"
		}
	}

	commandLine("bash", reportScript.absolutePath)
}
