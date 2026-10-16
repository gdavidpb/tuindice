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
