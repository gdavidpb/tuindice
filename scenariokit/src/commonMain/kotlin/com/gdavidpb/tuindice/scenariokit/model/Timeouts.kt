package com.gdavidpb.tuindice.scenariokit.model

/**
 * Step timeouts in milliseconds; scenarios use these names (as `Within.*` durations), never bare numbers.
 * A wait is for a condition and ends as soon as it holds, so a longer name only costs time when the step fails.
 */
object Timeouts {
	/** The default window of `ifVisible`: a quick look at whether something is there, never a wait. */
	const val Probe = 1_500L

	/** The default of `assertEnabled`: something that must already be true or be about to be. */
	const val Assert = 5_000L

	/**
	 * The budget each gesture gives its target to be visible (a `tap` also gives it to become enabled, in the same
	 * budget). Android's `foreground()` bounds its wait by it too.
	 */
	const val Action = 10_000L

	/** The default of `waitVisible`, `waitGone`, `waitBackgrounded`, `scrollUntilVisible` and `expectRequest`. */
	const val Wait = 20_000L

	/** For a wait that includes a delay the backend fixture imposes, such as a `Retry-After` or a slow mapping. */
	const val Long = 30_000L

	/** For the first screen after a launch, where the cold start of the app and its first sync are in the way. */
	const val Sync = 60_000L

	/**
	 * How long a typed field is re-read before the text counts as corrupted; it needs two reads in a row, one
	 * [PollInterval] apart, that show what was typed (a secure field: as many characters as were typed).
	 */
	const val TextReread = 3_000L

	/** The pause between two checks of a polling wait. */
	const val PollInterval = 200L

	/**
	 * The window that iOS gives XCTest to notice that the app left the foreground, counted from the moment
	 * `AppControl.confirmForeground` is asked. Measured, not chosen: see `docs/e2e-mediciones.md`.
	 */
	const val ForegroundSettle = 4_000L
}
