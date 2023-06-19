package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.base.utils.extension.daysDistance
import com.gdavidpb.tuindice.base.utils.extension.weeksDistance
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

			val actualDistance = futureDate.time.daysDistance()

			assertEquals(days, actualDistance)
		}
	}

	@Test
	fun testWeeksDistance() {
		(-12 until 12).forEach { week ->
			val futureDate = Calendar.getInstance().apply {
				add(Calendar.WEEK_OF_YEAR, week)
			}

			val actualDistance = futureDate.time.weeksDistance()

			assertEquals(week, actualDistance)
		}
	}
}