package com.gdavidpb.tuindice.pensum.utils

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

object CooldownTimes {
	// A cached pensum older than this is revalidated on the next screen entry or sync, so a
	// correction curated on the backend reaches installed apps within a day.
	val COOLDOWN_GET_PENSUM = 1.days.inWholeMilliseconds

	// After a failed automatic revalidation, the next automatic one waits this long; an explicit
	// refresh (pull, retry, first load) never waits.
	val COOLDOWN_RETRY_PENSUM = 1.hours.inWholeMilliseconds
}

// A stamp in the future (a clock moved back, a hand-edited value) is outside every window: one
// extra request is cheaper than a cache that never refreshes again.
internal fun isWithinWindow(sinceMillis: Long, nowMillis: Long, windowMillis: Long): Boolean {
	return nowMillis - sinceMillis in 0 until windowMillis
}
