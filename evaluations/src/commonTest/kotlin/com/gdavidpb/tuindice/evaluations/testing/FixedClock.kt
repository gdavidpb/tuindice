@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.testing

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** A clock that always says it is [iso] (an ISO-8601 instant with its offset). */
fun fixedClock(iso: String): Clock {
	val instant = Instant.parse(iso)

	return object : Clock {
		override fun now(): Instant = instant
	}
}
