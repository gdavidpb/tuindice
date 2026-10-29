package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleClash
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting

/**
 * The clashes of a schedule. Blocks are inclusive, as the grid draws them: two meetings clash when they share a
 * block of the same day, so meetings that only touch do not. A subject and section never clashes with itself.
 */
object ScheduleClashEngine {
	private val meetingOrder = compareBy<ScheduleMeeting>(
		{ meeting -> meeting.dayOfWeek },
		{ meeting -> meeting.startBlock },
		{ meeting -> meeting.endBlock },
		{ meeting -> meeting.subjectCode },
		{ meeting -> meeting.section },
		{ meeting -> meeting.attemptId },
		{ meeting -> meeting.classroom }
	)

	// By day, then by the block where the clash begins (the later of the two starts), then by the codes of its
	// two subjects; the meetings settle any tie, so two different clashes never compare equal.
	private val clashOrder = compareBy<ScheduleClash>(
		{ clash -> clash.day },
		{ clash -> maxOf(clash.first.startBlock, clash.second.startBlock) },
		{ clash -> clash.first.subjectCode },
		{ clash -> clash.second.subjectCode }
	)
		.thenComparator { a, b -> meetingOrder.compare(a.first, b.first) }
		.thenComparator { a, b -> meetingOrder.compare(a.second, b.second) }

	/**
	 * Each pair of clashing meetings, once. [ScheduleClash.first] is the meeting that starts first, and the
	 * clashes are ordered by day, then by the block where they begin, then by code. A meeting listed twice
	 * counts once.
	 */
	fun clashesOf(meetings: List<ScheduleMeeting>): List<ScheduleClash> {
		val sorted = meetings.distinct().sortedWith(meetingOrder)
		val clashes = sorted.indices.flatMap { index ->
			val first = sorted[index]

			sorted.drop(index + 1)
				.filter { second -> first.clashesWith(second) }
				.map { second -> ScheduleClash(day = first.dayOfWeek, first = first, second = second) }
		}

		return clashes.sortedWith(clashOrder)
	}

	private fun ScheduleMeeting.clashesWith(other: ScheduleMeeting): Boolean {
		val isSameSubjectAndSection = subjectCode == other.subjectCode && section == other.section

		return dayOfWeek == other.dayOfWeek &&
			startBlock <= other.endBlock &&
			other.startBlock <= endBlock &&
			!isSameSubjectAndSection
	}
}
