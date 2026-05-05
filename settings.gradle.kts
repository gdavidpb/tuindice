plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
	":app",
	":maincore",
	":base",
	":academiccore",
	":testkit",
	":persistence",
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
