package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.HistoricalBadge
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TermItemCurrentStateTest {
	@Test
	fun isCurrentTerm_returnsTrue_forOfficialCurrentTerms() {
		val term = termProjection(
			kind = TermKind.OFFICIAL_CURRENT
		)

		assertTrue(term.isCurrentTerm())
	}

	@Test
	fun isCurrentTerm_returnsFalse_forNonCurrentKinds() {
		val term = termProjection(
			kind = TermKind.OFFICIAL_HISTORICAL
		)

		assertFalse(term.isCurrentTerm())
	}

	@Test
	fun canDeleteTerm_returnsTrue_onlyForSyntheticTerms() {
		assertTrue(termProjection(kind = TermKind.SYNTHETIC).canDeleteTerm())
		assertFalse(termProjection(kind = TermKind.OFFICIAL_CURRENT).canDeleteTerm())
		assertFalse(termProjection(kind = TermKind.OFFICIAL_HISTORICAL).canDeleteTerm())
	}

	@Test
	fun isAttemptReadOnly_returnsTrue_forHistoricalTerms_evenWhenAttemptIsEditable() {
		val term = termProjection(kind = TermKind.OFFICIAL_HISTORICAL)

		assertTrue(term.isAttemptReadOnly(attemptProjection(editable = true)))
	}

	@Test
	fun isAttemptReadOnly_returnsAttemptEditability_forEditableTermKinds() {
		val term = termProjection(kind = TermKind.OFFICIAL_CURRENT)

		assertFalse(term.isAttemptReadOnly(attemptProjection(editable = true)))
		assertTrue(term.isAttemptReadOnly(attemptProjection(editable = false)))
	}
}

private fun termProjection(
	kind: TermKind
) = TermProjection(
	id = "term-id",
	label = "Term",
	startAtMillis = 1_000L,
	endAtMillis = 2_000L,
	order = 0,
	kind = kind,
	grade = 0.0,
	gradeSum = 0.0,
	credits = 0,
	creditsSum = 0,
	attempts = emptyList()
)

private fun attemptProjection(editable: Boolean) = AttemptProjection(
	id = "attempt-id",
	termId = "term-id",
	subjectCode = "MA1116",
	subjectName = "Calculo",
	credits = 4,
	sequenceInTerm = 0,
	gradingMode = AttemptGradingMode.NUMERIC,
	rawGradeToken = "",
	rawObservationText = "",
	score = AttemptScore.numeric(5),
	outcome = OfficialOutcome.APPROVED,
	badge = HistoricalBadge.NONE,
	editable = editable,
	synthetic = false,
	countsTowardTermAverage = true,
	countsTowardCumulativeAverage = true
)
