package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** The kit's named timeouts as durations, for the DSL. */
object Within {
	val Now: Duration = Timeouts.Now.milliseconds
	val Assert: Duration = Timeouts.Assert.milliseconds
	val Action: Duration = Timeouts.Action.milliseconds
	val Wait: Duration = Timeouts.Wait.milliseconds
	val Long: Duration = Timeouts.Long.milliseconds
	val Sync: Duration = Timeouts.Sync.milliseconds
}
