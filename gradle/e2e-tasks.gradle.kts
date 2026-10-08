import java.time.Duration

// Registration of the E2E tasks: the e2e* runs, the verifyE2e* verifiers (verifyE2eContract aggregates them), the
// launch-argument contract, the catalog sync and the iOS UI test build. Applied from the root build file
// (apply(from = ...)) and kept in a file of its own because the root build.gradle.kts is part of the E2E fingerprint (it sets the native compiler
// arguments and the classpath) while editing a verification task cannot change what a scenario does: this file is
// outside the fingerprint on purpose (e2e/scripts/shared/e2e-fingerprint.sh).

tasks.register<Exec>("verifyLaunchArgumentContract") {
	group = "verification"
	description = "Validates that debug launch arguments are declared once and never read in release builds."
	commandLine("bash", "${rootDir}/scripts/verify-launch-argument-contract.sh")
}

private val e2eCatalogJson = "e2e/catalog/scenarios.json"
private val e2eCatalogSwift = "iosApp/UITests/Generated/ScenarioTests.generated.swift"

tasks.register("syncE2eArtifacts") {
	group = "build setup"
	description = "Regenerates the versioned scenario catalog JSON and the generated XCUITest class list from the scenarios."
	dependsOn(":scenarios:testAndroidHostTest")

	val generated = layout.projectDirectory.dir("scenarios/build/e2e/catalog")
	val targets = mapOf(
		"scenarios.json" to layout.projectDirectory.file(e2eCatalogJson),
		"ScenarioTests.generated.swift" to layout.projectDirectory.file(e2eCatalogSwift)
	)

	doLast {
		targets.forEach { (name, target) ->
			val destination = target.asFile

			destination.parentFile.mkdirs()
			generated.file(name).asFile.copyTo(destination, overwrite = true)
		}
	}
}

tasks.register<Exec>("verifyE2eArtifactsFresh") {
	group = "verification"
	description = "Fails when the versioned scenario catalog or the generated XCUITest list differ from the scenarios."
	dependsOn(":scenarios:testAndroidHostTest")
	commandLine("bash", "${rootDir}/scripts/verify-e2e-artifacts.sh")
}

private val e2eHarnessScript = "${rootDir}/e2e/scripts/shared/e2e.py"
private val e2eBudgetMinutes = providers.environmentVariable("E2E_BUDGET_MINUTES")
	.map { it.toLongOrNull() ?: 120L }
	.orElse(120L)

// The Gradle timeout is the harness budget plus 30 minutes (twice the budget for both platforms), so that the harness ends
// by itself with its own exit code 4 before Gradle cuts it. Evidence depends on the generated artifacts being fresh: a
// scenario changed in Kotlin whose catalog JSON was not regenerated would otherwise be certified with the old steps.
// Running e2e.py directly does not go through this check.
private fun registerE2eRun(name: String, description: String, platform: String, mode: String, timeout: Provider<Duration>) {
	tasks.register<Exec>(name) {
		group = "verification"
		this.description = description
		this.timeout.set(timeout)
		if (mode == "evidence") {
			dependsOn("verifyE2eArtifactsFresh")
		}
		commandLine("python3", e2eHarnessScript, "run", "--platform", platform, "--mode", mode)
	}
}

registerE2eRun(
	"e2eAndroid", "Diagnostic E2E run on Android; E2E_SCENARIOS narrows it. Never produces evidence.",
	"android", "diagnose", e2eBudgetMinutes.map { Duration.ofMinutes(it + 30) }
)
registerE2eRun(
	"e2eIos", "Diagnostic E2E run on iOS; E2E_SCENARIOS narrows it. Never produces evidence.",
	"ios", "diagnose", e2eBudgetMinutes.map { Duration.ofMinutes(it + 30) }
)
registerE2eRun(
	"e2eEvidenceAndroid", "Local E2E evidence for Android: the whole catalog, accumulated per fingerprint, published as a status.",
	"android", "evidence", e2eBudgetMinutes.map { Duration.ofMinutes(it + 30) }
)
registerE2eRun(
	"e2eEvidenceIos", "Local E2E evidence for iOS: the whole catalog, accumulated per fingerprint, published as a status.",
	"ios", "evidence", e2eBudgetMinutes.map { Duration.ofMinutes(it + 30) }
)
registerE2eRun(
	"e2eEvidence", "Local E2E evidence for both platforms.",
	"all", "evidence", e2eBudgetMinutes.map { Duration.ofMinutes(2 * it + 30) }
)

tasks.register<Exec>("e2eStatus") {
	group = "verification"
	description = "Prints, per platform, the E2E ledger of the current fingerprint."
	timeout.set(Duration.ofMinutes(2))
	commandLine("python3", e2eHarnessScript, "status")
}

tasks.register<Exec>("e2eEnvCheck") {
	group = "verification"
	description = "Measures the host against the E2E environment thresholds."
	timeout.set(Duration.ofMinutes(1))
	commandLine("python3", e2eHarnessScript, "env-check")
}

tasks.register<Exec>("verifyE2eHarness") {
	group = "verification"
	description = "Runs the E2E harness checks: shell and Python syntax, the harness and skill unit tests, the fingerprint coverage, the line budgets and the vocabulary gate."
	timeout.set(Duration.ofMinutes(15))
	commandLine("bash", "${rootDir}/e2e/tools/tests/run-harness-tests.sh")
}

tasks.register<Exec>("verifyIosUiTestsBuild") {
	group = "verification"
	description = "Builds the host app and the TuIndiceUITests bundle and checks that the app carries no ScenarioKit."

	val isMacHost = System.getProperty("os.name").contains("Mac", ignoreCase = true)

	onlyIf { isMacHost }
	timeout.set(Duration.ofMinutes(40))
	commandLine("bash", "${rootDir}/e2e/scripts/ios/build.sh", "--for-testing-only")
}

tasks.register("verifyE2eContract") {
	group = "verification"
	description = "Aggregates the E2E contract: the kit and scenario host tests, the committed artifacts, the harness checks and the launch-argument contract."
	dependsOn(
		":scenariokit:testAndroidHostTest",
		":scenarios:testAndroidHostTest",
		"verifyE2eArtifactsFresh",
		"verifyE2eHarness",
		"verifyLaunchArgumentContract"
	)
}
