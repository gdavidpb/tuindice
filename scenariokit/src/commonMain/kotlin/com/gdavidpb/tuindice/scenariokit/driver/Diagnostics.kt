package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Platform

interface Diagnostics {
	val platform: Platform

	fun log(line: String)

	fun pause(ms: Long)

	/** Screenshot, hierarchy and platform log of the failure, wherever the runner keeps artifacts. */
	fun captureFailure(scenarioId: String, stepIndex: Int)

	/** Label of an OS dialog covering the app, or null when nothing is in front. */
	fun systemDialogInFront(): String?
}
