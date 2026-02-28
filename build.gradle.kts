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

tasks.register("verifyKmpTargets") {
	group = "verification"
	description = "Compiles KMP shared modules for Android and iOS simulator targets."

	dependsOn(
		":about:compileAndroidMain",
		":about:compileKotlinIosSimulatorArm64",
		":base:compileAndroidMain",
		":base:compileKotlinIosSimulatorArm64",
		":testkit:compileAndroidMain",
		":testkit:compileKotlinIosSimulatorArm64",
		":enrollmentproof:compileAndroidMain",
		":enrollmentproof:compileKotlinIosSimulatorArm64",
		":evaluations:compileAndroidMain",
		":evaluations:compileKotlinIosSimulatorArm64",
		":login:compileAndroidMain",
		":login:compileKotlinIosSimulatorArm64",
		":maincore:compileAndroidMain",
		":maincore:compileKotlinIosSimulatorArm64",
		":persistence:compileAndroidMain",
		":persistence:compileKotlinIosSimulatorArm64",
		":record:compileAndroidMain",
		":record:compileKotlinIosSimulatorArm64",
		":summary:compileAndroidMain",
		":summary:compileKotlinIosSimulatorArm64"
	)
}

tasks.register("verifyKmpSharedTests") {
	group = "verification"
	description = "Runs shared KMP tests on iOS x64 where available in CI/dev machines."

	dependsOn(
		":about:iosX64Test",
		":base:iosX64Test",
		":testkit:iosX64Test",
		":enrollmentproof:iosX64Test",
		":evaluations:iosX64Test",
		":login:iosX64Test",
		":record:iosX64Test",
		":summary:iosX64Test",
		":maincore:iosX64Test"
	)
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

	if (isMacHost && runHostBuild) {
		dependsOn(":maincore:linkDebugFrameworkIosSimulatorArm64")
	}

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

	if (isMacHost && runHostBuild) {
		dependsOn(":maincore:linkReleaseFrameworkIosSimulatorArm64")
	}

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

	if (isMacHost && runHostBuild && runHostDeviceE2E) {
		dependsOn(":maincore:linkReleaseFrameworkIosArm64")
	}

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
	val reportScript = file("${rootDir}/scripts/kmp/report-worktree-health.sh")

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
