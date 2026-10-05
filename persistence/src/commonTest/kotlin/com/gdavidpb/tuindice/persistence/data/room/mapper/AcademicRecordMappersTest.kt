package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordMappersTest {
	@Test
	fun academicTerm_roundTripsThroughRoomEntity_withTermKind() {
		val term = AcademicTerm(
			id = "term-1",
			periodYear = 2026,
			periodCode = AcademicTermPeriod.JAN_MAR,
			kind = TermKind.CURRENT
		)

		val roundTrip = listOf(term.toAcademicTermEntity())
			.toAcademicTerms(attempts = emptyList())
			.single()

		assertEquals(term, roundTrip)
	}

	@Test
	fun academicAttempt_roundTripsThroughRoomEntity_withScheduleSectionErrorsAndWithdrawn() {
		val attempt = AcademicAttempt(
			id = "attempt-1",
			subjectCode = "MA2115",
			subjectName = "Matematicas 3",
			credits = 4,
			section = 1,
			schedule = listOf(
				AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116"),
				AcademicScheduleEntry(dayOfWeek = 4, startBlock = 3, endBlock = 3)
			),
			enrollmentErrors = listOf("CHOQUE DE HORARIO"),
			withdrawn = true
		)

		val entity = attempt.toAcademicAttemptEntity(termId = "term-1", positionInTerm = 0)

		assertEquals(attempt, entity.toAcademicAttempt())
	}

	@Test
	fun academicAttempt_withoutScheduleFields_roundTripsAsAbsent() {
		val attempt = AcademicAttempt(
			id = "attempt-1",
			subjectCode = "MA2115",
			subjectName = "Matematicas 3",
			credits = 4
		)

		val entity = attempt.toAcademicAttemptEntity(termId = "term-1", positionInTerm = 0)

		assertEquals(null, entity.schedule)
		assertEquals(null, entity.withdrawn)
		assertEquals(attempt, entity.toAcademicAttempt())
	}

	@Test
	fun academicAttempt_roundTripsThroughRoomEntities_preservingPositionOrderAndStatus() {
		val term = AcademicTerm(
			id = "term-1",
			periodYear = 2026,
			periodCode = AcademicTermPeriod.APR_JUL,
			kind = TermKind.SYNTHETIC
		)
		val laterAttempt = AcademicAttempt(
			id = "attempt-2",
			subjectCode = "MAT2205",
			subjectName = "Ecuaciones Diferenciales",
			credits = 5,
			gradingMode = AttemptGradingMode.NUMERIC,
			academicScore = AttemptScore.numeric(5),
			academicOutcome = AttemptOutcome.APPROVED,
			academicBadge = AttemptBadge.NONE
		)
		val earlierAttempt = AcademicAttempt(
			id = "attempt-1",
			subjectCode = "MAT1203",
			subjectName = "Algebra",
			credits = 4,
			gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
			academicScore = AttemptScore.symbolic("A"),
			academicOutcome = AttemptOutcome.APPROVED,
			academicBadge = AttemptBadge.WITHOUT_EFFECT
		)

		val roundTrip = listOf(term.toAcademicTermEntity())
			.toAcademicTerms(
				attempts = listOf(
					laterAttempt.toAcademicAttemptEntity(
						termId = term.id,
						positionInTerm = 1
					),
					earlierAttempt.toAcademicAttemptEntity(
						termId = term.id,
						positionInTerm = 0
					)
				)
			)
			.single()

		assertEquals(listOf(earlierAttempt, laterAttempt), roundTrip.attempts)
	}

	@Test
	fun attemptOverride_roundTripsThroughRoomEntity_withStructuredScore() {
		val override = AttemptOverride(
			attemptId = "attempt-1",
			score = AttemptScore.symbolic("R"),
			outcome = AttemptOutcome.RETIRED,
			updatedAtMillis = 1_234L
		)

		val roundTrip = override
			.toAcademicAttemptOverrideEntity()
			.toAttemptOverride()

		assertEquals(override, roundTrip)
	}
}
