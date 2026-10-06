@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The clock handed to the data source moves with the test's virtual time, so a minute turns when
 * the test says so and nothing here waits for a real one.
 */
class ScheduleClockDataSourceTest {
	@Test
	fun when_observed_then_theMomentArrivesAtOnce_withoutWaitingForTheNextMinute() = runTest {
		val emitted = sourceAt(MondayNineOhFiveAndForty).observeNow().first()

		assertEquals(ScheduleNow(dayOfWeek = MONDAY, minuteOfDay = 9 * 60 + 5), emitted)
		// It reads first and sleeps after: no time went by to get the first value.
		assertEquals(0L, currentTime)
	}

	@Test
	fun when_theMinuteTurns_then_theNextMomentArrivesExactlyThen_andNotBefore() = runTest {
		val emitted = mutableListOf<ScheduleNow>()

		backgroundScope.launch {
			sourceAt(MondayNineOhFiveAndForty).observeNow().collect { now -> emitted += now }
		}

		// Twenty seconds are left of 09:05; one millisecond short of them nothing new is said.
		advanceTimeBy(20.seconds - 1.milliseconds)
		runCurrent()
		assertEquals(listOf(ScheduleNow(MONDAY, 9 * 60 + 5)), emitted)

		advanceTimeBy(1.milliseconds)
		runCurrent()
		assertEquals(listOf(ScheduleNow(MONDAY, 9 * 60 + 5), ScheduleNow(MONDAY, 9 * 60 + 6)), emitted)

		// From then on, once per minute.
		advanceTimeBy(60.seconds)
		runCurrent()
		assertEquals(ScheduleNow(MONDAY, 9 * 60 + 7), emitted.last())
		assertEquals(3, emitted.size)
	}

	@Test
	fun when_readOnEachDayOfTheWeek_then_theDayUsesTheBackendsCode_fromSundayToSaturday() = runTest {
		// 2026-10-04 is a Sunday; the backend counts 1 = Sunday ... 7 = Saturday.
		val codes = (0..6).map { offset ->
			sourceAt(SundayNoon + offset.days).observeNow().first().dayOfWeek
		}

		assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), codes)
	}

	@Test
	fun when_theDeviceChangesItsTimeZone_then_theNextTickFollowsIt() = runTest {
		var zone: TimeZone = TimeZone.UTC
		val emitted = mutableListOf<ScheduleNow>()

		backgroundScope.launch {
			sourceAt(SundayNoon, timeZone = { zone }).observeNow().collect { now -> emitted += now }
		}

		runCurrent()
		assertEquals(ScheduleNow(dayOfWeek = 1, minuteOfDay = 12 * 60), emitted.single())

		// Thirteen hours ahead: the same instant is already Monday there.
		zone = FixedOffsetTimeZone(UtcOffset(hours = 13))
		advanceTimeBy(60.seconds)
		runCurrent()

		assertEquals(ScheduleNow(dayOfWeek = MONDAY, minuteOfDay = 60 + 1), emitted.last())
	}

	@Test
	fun when_twoScreensListen_then_eachHasItsOwnTicker_thatStopsWithItsScreen() = runTest {
		val source = sourceAt(MondayNineOhFiveAndForty)
		val first = mutableListOf<ScheduleNow>()
		val second = mutableListOf<ScheduleNow>()

		val firstScreen = backgroundScope.launch { source.observeNow().collect { now -> first += now } }
		val secondScreen = backgroundScope.launch { source.observeNow().collect { now -> second += now } }

		runCurrent()

		// Nothing is shared: each collector got the present moment from a ticker of its own.
		assertEquals(first, second)
		assertEquals(1, first.size)

		firstScreen.cancelAndJoin()
		advanceTimeBy(60.seconds)
		runCurrent()

		assertFalse(firstScreen.isActive)
		assertTrue(secondScreen.isActive, "leaving one screen does not stop the other's clock")
		assertEquals(1, first.size)
		assertEquals(2, second.size)
	}

	// A clock that starts at [start] and moves with the test's virtual time.
	private fun TestScope.sourceAt(
		start: Instant,
		timeZone: () -> TimeZone = { TimeZone.UTC }
	) = ScheduleClockDataSource(
		clock = object : Clock {
			override fun now(): Instant = start + currentTime.milliseconds
		},
		timeZone = timeZone
	)

	private companion object {
		const val MONDAY = 2

		val SundayNoon = Instant.parse("2026-10-04T12:00:00Z")
		val MondayNineOhFiveAndForty = Instant.parse("2026-10-05T09:05:40Z")
	}
}
