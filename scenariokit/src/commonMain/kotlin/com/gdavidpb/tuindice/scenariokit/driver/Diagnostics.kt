package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Platform

interface Diagnostics {
	val platform: Platform

	/** One line of the driver's log: logcat on Android, the driver log (printed with `E2E_TRACE`) on iOS. */
	fun log(line: String)

	/**
	 * Why the driver refused the gesture or text entry that just answered false (the frame never settled, the point is
	 * under the keyboard, the element went away, the keys were not injected), or null when it gave no reason. The
	 * interpreter reads it right after a false and puts it in the message of the failing step, so the reason is in
	 * `result.json` and not only in the driver's log. Every [Gestures] and [TextEntry] call clears it first.
	 */
	fun lastRefusal(): String?

	/**
	 * Blocks the calling thread for [ms]. The interpreter's polling waits through it between two checks; a scenario
	 * has no step to pause, because a wait is always for a condition.
	 */
	fun pause(ms: Long)

	/**
	 * Screenshot, hierarchy and platform log of the failure, wherever the runner keeps artifacts: files of the attempt
	 * on Android, attachments of the test on iOS. The interpreter calls it once, for the first failing step.
	 */
	fun captureFailure(scenarioId: String, stepIndex: Int)

	/**
	 * Label of an OS dialog covering the app, or null when nothing is in front. The interpreter asks it after a failed
	 * step: a dialog turns the failure into `SYSTEM_DIALOG`, which the harness classifies as an environment failure.
	 */
	fun systemDialogInFront(): String?
}
