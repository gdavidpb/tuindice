package com.gdavidpb.tuindice.scenariokit.model

/** Step timeouts in milliseconds; scenarios use these names, never bare numbers. */
object Timeouts {
	const val Probe = 1_500L
	const val Assert = 5_000L
	const val Action = 10_000L
	const val Wait = 20_000L
	const val Long = 30_000L
	const val Sync = 60_000L

	/** How long a typed, non-secure field is re-read before the text counts as corrupted. */
	const val TextReread = 3_000L
	const val PollInterval = 200L
	const val SettleInterval = 150L
}
