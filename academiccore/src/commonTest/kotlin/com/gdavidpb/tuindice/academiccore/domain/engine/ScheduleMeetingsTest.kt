package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleMeetingsTest {
	private companion object {
		const val MONDAY = 2
		const val WEDNESDAY = 4
	}

	@Test
	fun validMeetings_ofAnAttempt_keepOnlyPlaceableEntriesTrimmedAndWithoutDuplicates() {
		val attempt = AcademicAttempt(
			id = "a1",
			subjectCode = "MA1111",
			subjectName = "Math",
			credits = 3,
			section = 2,
			schedule = listOf(
				AcademicScheduleEntry(MONDAY, 1, 2, " B-101 "),
				AcademicScheduleEntry(MONDAY, 1, 2, "B-101"),
				AcademicScheduleEntry(WEDNESDAY, 5, 4, "B-101"),
				AcademicScheduleEntry(9, 1, 2)
			)
		)

		assertEquals(
			listOf(ScheduleMeeting("a1", "MA1111", 2, MONDAY, 1, 2, "B-101")),
			attempt.validMeetings()
		)
	}

	@Test
	fun validMeetings_ofAWithdrawnOrUnscheduledAttempt_areEmpty() {
		val schedule = listOf(AcademicScheduleEntry(MONDAY, 1, 2))
		val withdrawn = AcademicAttempt("a", "MA1111", "Math", 3, schedule = schedule, withdrawn = true)
		val unscheduled = AcademicAttempt("b", "MA1112", "Math 2", 3)

		assertTrue(withdrawn.validMeetings().isEmpty())
		assertTrue(unscheduled.validMeetings().isEmpty())
	}

	@Test
	fun validMeetings_ofAProjection_followTheSameRules() {
		fun projection(withdrawn: Boolean) = AttemptProjection(
			id = "p1",
			subjectCode = "CI2525",
			subjectName = "Algorithms",
			credits = 4,
			gradingMode = AttemptGradingMode.NUMERIC,
			score = AttemptScore.empty(),
			outcome = AttemptOutcome.PENDING,
			badge = AttemptBadge.NONE,
			schedule = listOf(AcademicScheduleEntry(WEDNESDAY, 3, 3, "")),
			withdrawn = withdrawn
		)

		assertEquals(
			listOf(ScheduleMeeting("p1", "CI2525", null, WEDNESDAY, 3, 3, "")),
			projection(withdrawn = false).validMeetings()
		)
		assertTrue(projection(withdrawn = true).validMeetings().isEmpty())
	}

	@Test
	fun validMeetings_ofAProjection_trimTheClassroomDropDuplicatesAndMalformedEntries() {
		val projection = AttemptProjection(
			id = "p2",
			subjectCode = "CI2526",
			subjectName = "Algorithms 2",
			credits = 4,
			gradingMode = AttemptGradingMode.NUMERIC,
			score = AttemptScore.empty(),
			outcome = AttemptOutcome.PENDING,
			badge = AttemptBadge.NONE,
			section = 3,
			schedule = listOf(
				AcademicScheduleEntry(MONDAY, 1, 2, "  B-101  "),
				AcademicScheduleEntry(MONDAY, 1, 2, "B-101"),
				AcademicScheduleEntry(WEDNESDAY, 5, 4, "B-101"),
				AcademicScheduleEntry(0, 1, 2, "B-101"),
				AcademicScheduleEntry(MONDAY, 0, 2, "B-101"),
				AcademicScheduleEntry(MONDAY, 23, 25, "B-101")
			)
		)

		assertEquals(
			listOf(ScheduleMeeting("p2", "CI2526", 3, MONDAY, 1, 2, "B-101")),
			projection.validMeetings()
		)
	}
}
