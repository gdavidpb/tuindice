plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
	":app",
	":maincore",
	":base",
	":testkit",
	":persistence",
	":login",
	":about",
	":summary",
	":record",
	":enrollmentproof",
	":evaluations"
)
