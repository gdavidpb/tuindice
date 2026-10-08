package com.gdavidpb.tuindice.scenariorunner

import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry

/** Instrumentation arguments that steer one run; the harness passes them with `-e`. */
object RunConfig {
	private const val DEFAULT_WIREMOCK_URL = "http://10.0.2.2:18626"

	private val arguments: Bundle get() = InstrumentationRegistry.getArguments()

	/** Comma-separated scenario ids to run; empty runs everything. */
	val scenarioIds: List<String>
		get() = arguments.getString("scenario").orEmpty().split(',').map(String::trim).filter(String::isNotEmpty)

	val scenarioModule: String? get() = arguments.getString("scenarioModule")?.takeIf(String::isNotBlank)

	val wiremockUrl: String get() = arguments.getString("wiremockUrl")?.takeIf(String::isNotBlank) ?: DEFAULT_WIREMOCK_URL

	/**
	 * Base directory for per-scenario output; null means the test APK's own `files/e2e`.
	 * When set it replaces that base, and each scenario still writes into its own `<id>` subdirectory.
	 */
	val outputDir: String? get() = arguments.getString("e2eOutputDir")?.takeIf(String::isNotBlank)

	/** True when the run is narrowed to some scenarios, in which case the driver contract is skipped. */
	val isFiltered: Boolean get() = scenarioIds.isNotEmpty() || scenarioModule != null
}
