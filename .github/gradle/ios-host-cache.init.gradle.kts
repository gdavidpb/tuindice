import org.gradle.api.tasks.Exec

private fun iosFrameworkBinary(rootDir: File, targetName: String, buildType: String): File =
	rootDir.resolve("maincore/build/bin/$targetName/${buildType.lowercase()}Framework/maincore.framework/maincore")

private fun shouldUseCachedIosFramework(rootDir: File, targetName: String, buildType: String): Boolean =
	System.getenv("TUINDICE_IOS_USE_CACHED_FRAMEWORK") == "1" &&
		iosFrameworkBinary(rootDir, targetName, buildType).isFile

gradle.projectsEvaluated {
	rootProject.tasks.named("verifyIosHostBuildDeviceRelease", Exec::class.java).configure {
		val useCachedReleaseFramework = shouldUseCachedIosFramework(rootProject.rootDir, "iosArm64", "Release")

		if (!useCachedReleaseFramework) {
			dependsOn(":maincore:linkReleaseFrameworkIosArm64")
		}
		dependsOn(":maincore:syncComposeResourcesForIos")

		environment("SKIP_FRAMEWORK_BUILD", "1")
		environment(
			"CODE_SIGNING_ALLOWED",
			System.getenv("TUINDICE_IOS_HOST_DEVICE_CODE_SIGNING_ALLOWED") ?: "NO"
		)

		doFirst {
			if (useCachedReleaseFramework) {
				logger.lifecycle("Using cached iOS device release framework for verifyIosHostBuildDeviceRelease.")
			}
		}
	}
}
