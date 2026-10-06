package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateTest {
	@Test
	fun testDaysDistance() {
		(-7 until 7).forEach { days ->
			val futureDate = Calendar.getInstance().apply {
				add(Calendar.DAY_OF_YEAR, days)
			}

			val actualDistance = futureDate.timeInMillis.daysToNow()

			assertEquals(days, actualDistance)
		}
	}
}
