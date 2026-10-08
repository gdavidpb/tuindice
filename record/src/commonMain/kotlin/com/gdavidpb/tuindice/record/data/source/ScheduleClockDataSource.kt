@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.domain.model.ScheduleNow
import com.gdavidpb.tuindice.record.domain.repository.ScheduleClockRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60
private const val DAYS_PER_WEEK = 7

/**
 * The app's clock (the one Koin binds: the system one, or the frozen one of a debug build), read
 * once a minute. It keeps nothing: every collector gets its own ticker, which stops with the screen
 * that was listening. The clock and the time zone come in so a test or a debug build can say what
 * time it is; the time zone is the device's unless told otherwise. A frozen clock does not move: the
 * ticker keeps waking up, at most once a minute, and the same value is not emitted twice.
 */
class ScheduleClockDataSource(
	private val clock: Clock,
	// Asked on every tick: a traveller's schedule follows the device.
	private val timeZone: () -> TimeZone = TimeZone::currentSystemDefault
) : ScheduleClockRepository {
	override fun observeNow(): Flow<ScheduleNow> {
		return flow {
			while (true) {
				val instant = clock.now()

				emit(instant.toScheduleNow())
				// Sleep up to the start of the next minute, so the schedule moves when the clock does.
				delay(MILLIS_PER_MINUTE - instant.toEpochMilliseconds() % MILLIS_PER_MINUTE)
			}
		}.distinctUntilChanged()
	}

	private fun Instant.toScheduleNow(): ScheduleNow {
		val dateTime = toLocalDateTime(timeZone())

		return ScheduleNow(
			// ISO counts from Monday (1) to Sunday (7); the backend counts from Sunday (1) to Saturday (7).
			dayOfWeek = dateTime.dayOfWeek.isoDayNumber % DAYS_PER_WEEK + 1,
			minuteOfDay = dateTime.hour * MINUTES_PER_HOUR + dateTime.minute
		)
	}
}
