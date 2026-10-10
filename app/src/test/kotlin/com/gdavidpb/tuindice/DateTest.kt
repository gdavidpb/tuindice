package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DateTest {
	@Test
	fun testDaysDistance() {
		(-7 until 7).forEach { days ->
			val futureDate = Calendar.getInstance().apply {
				add(Calendar.DAY_OF_YEAR, days)
			}

			val actualDistance = futureDate.timeInMillis.daysToNow(Clock.System)

			assertEquals(days, actualDistance)
		}
	}
}
