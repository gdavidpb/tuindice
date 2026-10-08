package com.gdavidpb.tuindice.scenariokit.driver

import com.gdavidpb.tuindice.scenariokit.model.Platform

interface Diagnostics {
	val platform: Platform

	/**
	 * One line of the driver's log. Both drivers append every line to `driver.log` of the scenario's output directory
	 * the moment it is written, so the file survives a run that hangs and is killed, and the lines the interpreter
	 * writes (one per step) share it with the ones the driver writes about what it did on its own (a gesture refused,
	 * the app brought back to the front, an alert dismissed). Android also sends them to logcat; iOS also prints them
	 * with `E2E_TRACE`. A log that throws never costs the run its result.
	 */
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
	 * on Android (with logcat), attachments of the test on iOS. The interpreter calls it once, for the first failing
	 * step.
	 */
	fun captureFailure(scenarioId: String, stepIndex: Int)

	/**
	 * Label of an OS dialog covering the app, or null when nothing is in front. The interpreter asks it after a failed
	 * step: a dialog turns the failure into `SYSTEM_DIALOG`, which the harness classifies as an environment failure.
	 */
	fun systemDialogInFront(): String?
}
