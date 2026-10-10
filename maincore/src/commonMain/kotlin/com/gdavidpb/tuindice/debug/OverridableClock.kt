package com.gdavidpb.tuindice.debug

import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Debug clock that lets the `NOW` launch argument freeze "now". Only the debug variant
 * modules bind it; release binds the system clock and nothing sets [fixedNow] there.
 */
@OptIn(ExperimentalTime::class)
class OverridableClock(
	private val delegate: Clock = Clock.System
) : Clock {
	@Volatile
	var fixedNow: Instant? = null

	override fun now(): Instant = fixedNow ?: delegate.now()
}
