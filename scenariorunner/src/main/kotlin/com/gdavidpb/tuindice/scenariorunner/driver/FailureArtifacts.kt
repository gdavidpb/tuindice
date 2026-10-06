package com.gdavidpb.tuindice.scenariorunner.driver

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.gdavidpb.tuindice.scenariorunner.RunConfig
import java.io.File

/**
 * Where a scenario leaves its output: `result.json`, and on failure a screenshot, the view
 * hierarchy and the logcat. The default is the test APK's `files/e2e/<id>`, readable without
 * root through `run-as`; `e2eOutputDir` replaces the `files/e2e` base.
 */
internal object FailureArtifacts {
	/** Empties and returns the output directory of [scenarioId]. */
	fun reset(scenarioId: String): File =
		directory(scenarioId).also { dir ->
			dir.deleteRecursively()
			dir.mkdirs()
		}

	fun writeResult(scenarioId: String, json: String) {
		File(directory(scenarioId).also { it.mkdirs() }, "result.json").writeText(json)
	}

	fun capture(device: UiDevice, scenarioId: String, stepIndex: Int) {
		val dir = directory(scenarioId).also { it.mkdirs() }
		val prefix = "$scenarioId-$stepIndex"

		runCatching { device.takeScreenshot(File(dir, "$prefix.png")) }
		runCatching { device.dumpWindowHierarchy(File(dir, "$prefix.uix")) }
		runCatching { File(dir, "$prefix.logcat").writeText(device.executeShellCommand("logcat -d -v threadtime")) }
	}

	private fun directory(scenarioId: String): File {
		val base = RunConfig.outputDir?.let(::File)
			?: File(InstrumentationRegistry.getInstrumentation().context.filesDir, "e2e")

		return File(base, scenarioId)
	}
}
