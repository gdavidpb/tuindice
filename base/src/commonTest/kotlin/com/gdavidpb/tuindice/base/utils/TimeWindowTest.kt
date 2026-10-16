package com.gdavidpb.tuindice.base.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeWindowTest {
	@Test
	fun isWithinWindow_isHalfOpen_andTreatsAFutureStampAsOutside() {
		data class Case(val description: String, val since: Long, val within: Boolean)

		val now = 1_000_000L
		val window = 1_000L
		val cases = listOf(
			Case("stamped right now", since = now, within = true),
			Case("just inside the window", since = now - window + 1, within = true),
			Case("exactly one window old", since = now - window, within = false),
			Case("older than the window", since = now - 5 * window, within = false),
			Case("stamped in the future", since = now + 1, within = false)
		)

		cases.forEach { case ->
			assertEquals(
				case.within,
				isWithinWindow(sinceMillis = case.since, nowMillis = now, windowMillis = window),
				case.description
			)
		}
	}
}
