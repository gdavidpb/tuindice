package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleBlockClock
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMoment
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

/**
 * The rules over the weekly meetings of the subjects: which entries are valid, what happens on a
 * day, and which class is in progress or comes next. Days follow the model's convention
 * (1 = Sunday ... 7 = Saturday) and the clock of a block is [ScheduleBlockClock].
 */
object AcademicScheduleEngine {
	const val FIRST_BLOCK = 1

	// Far above the 11 blocks the university has used so far: anything beyond it is a malformed entry,
	// and drawing it would stretch a grid into dozens of empty rows.
	const val LAST_BLOCK = 24

	private const val FIRST_DAY = 1
	private const val LAST_DAY = 7
	private const val DAYS_PER_WEEK = 7
	private const val MAX_DAYS_AHEAD = 6
	private val SEARCH_DAYS_AHEAD = 1..MAX_DAYS_AHEAD

	private val meetingOrder = compareBy(ScheduleMeeting::startBlock, ScheduleMeeting::subjectCode)

	/**
	 * Whether [entry] can be placed: a day the week has and a block range that makes sense (it starts
	 * at [FIRST_BLOCK] or later, does not end before it starts and ends by [LAST_BLOCK]). A malformed
	 * entry is dropped rather than shown: a wrong cell is worse than none.
	 */
	fun isPlaceable(entry: AcademicScheduleEntry): Boolean {
		return entry.dayOfWeek in FIRST_DAY..LAST_DAY &&
			entry.startBlock >= FIRST_BLOCK &&
			entry.endBlock >= entry.startBlock &&
			entry.endBlock <= LAST_BLOCK
	}

	/** The meetings that happen on [dayOfWeek], by start block and then by subject code. */
	fun meetingsOn(meetings: List<ScheduleMeeting>, dayOfWeek: Int): List<ScheduleMeeting> {
		return meetings
			.filter { meeting -> meeting.dayOfWeek == dayOfWeek }
			.sortedWith(meetingOrder)
	}

	/**
	 * The class in progress at [minuteOfDay] of [dayOfWeek]: the meeting whose block range contains the
	 * block the clock is in (the first by start block and code when two overlap). Otherwise the next
	 * one: the first still to start today or, when none is left, the first of the next day with
	 * classes within the following six days. Null when there is no meeting to speak of.
	 */
	fun currentOrNext(
		meetings: List<ScheduleMeeting>,
		dayOfWeek: Int,
		minuteOfDay: Int
	): ScheduleMoment? {
		val today = meetingsOn(meetings, dayOfWeek)
		// 0 before block 1: nothing is in progress and every meeting of the day is still to come.
		val currentBlock = currentBlockAt(minuteOfDay)
		val inProgress = today.firstOrNull { meeting -> currentBlock in meeting.startBlock..meeting.endBlock }

		if (inProgress != null) return ScheduleMoment.InProgress(inProgress)

		val laterToday = today.firstOrNull { meeting -> meeting.startBlock > currentBlock }

		return if (laterToday != null) {
			ScheduleMoment.Next(meeting = laterToday, daysAhead = 0)
		} else {
			nextOnFollowingDays(meetings, dayOfWeek)
		}
	}

	/**
	 * The first meeting (lowest start block) among [attemptMeetings] on the weekday of [date], or null
	 * when the attempt has none that day.
	 */
	fun meetingFor(attemptMeetings: List<ScheduleMeeting>, date: LocalDate): ScheduleMeeting? {
		val dayOfWeek = dayOfWeekOf(date)

		return attemptMeetings
			.filter { meeting -> meeting.dayOfWeek == dayOfWeek }
			.minWithOrNull(meetingOrder)
	}

	/** The model's day (1 = Sunday ... 7 = Saturday) of [date]. */
	fun dayOfWeekOf(date: LocalDate): Int {
		return date.dayOfWeek.isoDayNumber % DAYS_PER_WEEK + FIRST_DAY
	}

	private fun nextOnFollowingDays(meetings: List<ScheduleMeeting>, dayOfWeek: Int): ScheduleMoment.Next? {
		return SEARCH_DAYS_AHEAD.firstNotNullOfOrNull { daysAhead ->
			val day = (dayOfWeek - FIRST_DAY + daysAhead) % DAYS_PER_WEEK + FIRST_DAY

			meetingsOn(meetings, day).firstOrNull()
				?.let { meeting -> ScheduleMoment.Next(meeting = meeting, daysAhead = daysAhead) }
		}
	}

	// The block being taught at the minute, or 0 while block 1 has not started.
	private fun currentBlockAt(minuteOfDay: Int): Int {
		val minutesSinceFirstBlock = minuteOfDay - ScheduleBlockClock.FIRST_BLOCK_START_MINUTE

		return if (minutesSinceFirstBlock < 0) 0 else minutesSinceFirstBlock / ScheduleBlockClock.BLOCK_MINUTES + 1
	}
}
