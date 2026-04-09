package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordRoomMappersTest {
	@Test
	fun academicTerm_roundTripsThroughRoomEntity_withTermKind() {
		val term = AcademicTerm(
			id = "term-1",
			label = "Enero - Marzo 2026",
			startAtMillis = 1_000L,
			endAtMillis = 2_000L,
			kind = TermKind.OFFICIAL_CURRENT
		)

		val roundTrip = listOf(term.toAcademicTermEntity())
			.toAcademicTerms(attempts = emptyList())
			.single()

		assertEquals(term, roundTrip)
	}

	@Test
	fun academicAttempt_roundTripsThroughRoomEntities_preservingPositionOrderAndStatus() {
		val term = AcademicTerm(
			id = "term-1",
			label = "Synthetic Term",
			startAtMillis = 1_000L,
			endAtMillis = 2_000L,
			kind = TermKind.SYNTHETIC
		)
		val laterAttempt = AcademicAttempt(
			id = "attempt-2",
			subjectCode = "MAT2205",
			subjectName = "Ecuaciones Diferenciales",
			credits = 5,
			gradingMode = AttemptGradingMode.NUMERIC,
			officialScore = AttemptScore.numeric(5),
			officialOutcome = AttemptOutcome.APPROVED,
			officialBadge = AttemptBadge.NONE
		)
		val earlierAttempt = AcademicAttempt(
			id = "attempt-1",
			subjectCode = "MAT1203",
			subjectName = "Algebra",
			credits = 4,
			gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
			officialScore = AttemptScore.symbolic("A"),
			officialOutcome = AttemptOutcome.APPROVED,
			officialBadge = AttemptBadge.WITHOUT_EFFECT
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
