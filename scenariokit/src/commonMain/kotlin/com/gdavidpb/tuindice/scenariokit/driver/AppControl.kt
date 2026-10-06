package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec

/**
 * Lifecycle of the app under test. Implementations never throw and never clear the
 * app's state: wiping it is the harness's job, before the runner starts.
 */
interface AppControl {
	/** Cold-starts the app with [spec]'s arguments. */
	fun launch(spec: LaunchSpec): Boolean

	/** Brings the already running app back to the foreground. */
	fun foreground(): Boolean

	fun terminate()

	fun isForeground(): Boolean
}
