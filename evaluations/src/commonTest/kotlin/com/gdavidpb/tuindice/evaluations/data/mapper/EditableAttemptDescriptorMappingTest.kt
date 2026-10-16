package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditableAttemptDescriptorMappingTest {
	private fun entity(schedule: String?, withdrawn: Boolean? = null) = AcademicAttemptEntity(
		id = "attempt-1",
		termId = "term-1",
		subjectCode = "CI5311",
		subjectName = "Algoritmos",
		credits = 4,
		positionInTerm = 0,
		gradingMode = "NUMERIC",
		scoreKind = "NONE",
		academicOutcome = "PENDING",
		academicBadge = "NONE",
		section = 1,
		withdrawn = withdrawn,
		schedule = schedule
	)

	// The only link between the stored schedule and what an evaluation inherits: leaving it out
	// compiles and every row would silently lose its place.
	@Test
	fun toLocalEditableAttemptDescriptor_carriesTheValidMeetingsOfTheAttempt() {
		val descriptor = entity(
			schedule = """[
				{"day_of_week":2,"start_block":1,"end_block":2,"classroom":" MYS-116 "},
				{"day_of_week":9,"start_block":1,"end_block":2,"classroom":"X"}
			]"""
		).toLocalEditableAttemptDescriptor()

		assertEquals(
			listOf(
				ScheduleMeeting(
					attemptId = "attempt-1",
					subjectCode = "CI5311",
					section = 1,
					dayOfWeek = 2,
					startBlock = 1,
					endBlock = 2,
					classroom = "MYS-116"
				)
			),
			descriptor.meetings
		)
		assertEquals(descriptor.meetings, descriptor.toEditableAttemptDescriptor().meetings)
	}

	@Test
	fun toLocalEditableAttemptDescriptor_withoutASchedule_hasNoMeetings() {
		assertTrue(entity(schedule = null).toLocalEditableAttemptDescriptor().meetings.isEmpty())
	}

	@Test
	fun toLocalEditableAttemptDescriptor_ofAWithdrawnAttempt_hasNoMeetings() {
		val descriptor = entity(
			schedule = """[{"day_of_week":2,"start_block":1,"end_block":2}]""",
			withdrawn = true
		).toLocalEditableAttemptDescriptor()

		assertTrue(descriptor.meetings.isEmpty())
	}

	@Test
	fun toLocalEditableAttemptDescriptor_withAnUnreadableSchedule_hasNoMeetingsAndDoesNotFail() {
		val descriptor = entity(schedule = "not json").toLocalEditableAttemptDescriptor()

		assertTrue(descriptor.meetings.isEmpty())
		assertEquals("CI5311", descriptor.code)
	}
}
