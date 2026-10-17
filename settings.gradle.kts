plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Fixed so the generated Compose resource packages (tuindice.<module>.generated.resources) do not
// follow the checkout directory name, which differs in worktrees.
rootProject.name = "tuindice"

include(
	":app",
	":maincore",
	":base",
	":academiccore",
	":chatcontract",
	":testkit",
	":scenariokit",
	":scenariorunner",
	":scenarios",
	":persistence",
	":security",
	":auth",
	":about",
	":summary",
	":record",
	":enrollmentproof",
	":evaluations",
	":subjects",
	":wizard",
	":pensum"
)
