package com.gdavidpb.tuindice.scenarios

import java.io.File

/** The source files of the two drivers, as the static tests of their rules read them. */
internal object DriverSourceFiles {
	/** Every Kotlin file of the Android driver and its probes. */
	val android: List<File> = RepoFiles.file("scenariorunner/src/main").walkTopDown()
		.filter { it.extension == "kt" }
		.toList()

	/** Every Swift file of the iOS driver. */
	val ios: List<File> = RepoFiles.file("iosApp/UITests")
		.listFiles { file -> file.extension == "swift" }
		.orEmpty()
		.toList()
}
