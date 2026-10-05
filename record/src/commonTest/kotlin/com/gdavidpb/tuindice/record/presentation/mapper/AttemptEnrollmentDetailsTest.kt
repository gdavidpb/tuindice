package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttemptEnrollmentDetailsTest {
	@Test
	fun when_sectionAndOneClassroom_then_showsBoth() {
		val attempt = attempt(
			section = 1,
			schedule = listOf(entry(classroom = "MYS-116"), entry(day = 4, classroom = "MYS-116"), entry(day = 5))
		)

		assertEquals("Sección 1 · MYS-116", attempt.toEnrollmentDetailText(texts()))
	}

	@Test
	fun when_classroomsDisagree_then_showsOnlyTheSection() {
		val attempt = attempt(
			section = 2,
			schedule = listOf(entry(classroom = "MYS-116"), entry(day = 4, classroom = "EDC-22"))
		)

		assertEquals("Sección 2", attempt.toEnrollmentDetailText(texts()))
	}

	@Test
	fun when_onlyAClassroom_then_showsIt() {
		val attempt = attempt(section = null, schedule = listOf(entry(classroom = " MYS-116 ")))

		assertEquals("MYS-116", attempt.toEnrollmentDetailText(texts()))
	}

	@Test
	fun when_noSectionNorClassroom_then_showsNothing() {
		assertNull(attempt(section = null, schedule = listOf(entry())).toEnrollmentDetailText(texts()))
		assertNull(attempt(section = null, schedule = null).toEnrollmentDetailText(texts()))
	}

	@Test
	fun when_universityErrorIsInCapitals_then_showsTheFirstOneInSentenceCase() {
		val attempt = attempt(enrollmentErrors = listOf("CHOQUE DE HORARIO", "OTRO"))

		assertEquals("Choque de horario", attempt.toEnrollmentErrorText())
		assertNull(attempt().toEnrollmentErrorText())
		assertNull(attempt(enrollmentErrors = listOf("  ")).toEnrollmentErrorText())
	}

	private fun texts() = RecordMapperTexts(
		termGrade = { "" },
		termGradeSum = { "" },
		termCredits = { "" },
		termAttemptGrade = { "" },
		termAttemptCredits = { "" },
		termAttemptSection = { section -> "Sección $section" },
		termAttemptSectionClassroom = { section, classroom -> "Sección $section · $classroom" }
	)

	private fun entry(day: Int = 2, classroom: String = "") = AcademicScheduleEntry(
		dayOfWeek = day,
		startBlock = 1,
		endBlock = 2,
		classroom = classroom
	)

	private fun attempt(
		section: Int? = null,
		schedule: List<AcademicScheduleEntry>? = null,
		enrollmentErrors: List<String>? = null
	) = AttemptProjection(
		id = "attempt-1",
		subjectCode = "MA2115",
		subjectName = "Matematicas 3",
		credits = 4,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		section = section,
		schedule = schedule,
		enrollmentErrors = enrollmentErrors
	)
}
