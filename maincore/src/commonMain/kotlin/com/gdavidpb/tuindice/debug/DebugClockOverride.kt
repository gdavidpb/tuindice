package com.gdavidpb.tuindice.debug

import org.koin.core.Koin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Freezes the app clock at [instant]; only the debug variant modules bind a clock that allows it. */
@OptIn(ExperimentalTime::class)
fun Koin.freezeDebugClock(instant: Instant) {
	val clock = get<Clock>()

	check(clock is OverridableClock) {
		"Clock overrides require OverridableClock."
	}

	clock.fixedNow = instant
}
