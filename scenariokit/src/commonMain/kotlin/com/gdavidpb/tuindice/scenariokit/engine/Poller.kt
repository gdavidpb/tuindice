package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.Diagnostics
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.TimeSource

/** Repeats a check until it holds or the timeout elapses, pausing through the driver between tries. */
internal class Poller(private val driver: Diagnostics, private val timeSource: TimeSource) {
	fun until(timeoutMs: Long, intervalMs: Long = Timeouts.PollInterval, condition: () -> Boolean): Boolean {
		val mark = timeSource.markNow()
		var satisfied = condition()
		while (!satisfied && mark.elapsedNow().inWholeMilliseconds < timeoutMs) {
			driver.pause(intervalMs)
			satisfied = condition()
		}
		return satisfied
	}
}
