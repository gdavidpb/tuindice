plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
	":app",
	":maincore",
	":base",
	":persistence",
	":login",
	":about",
	":summary",
	":record",
	":enrollmentproof",
	":evaluations"
)
