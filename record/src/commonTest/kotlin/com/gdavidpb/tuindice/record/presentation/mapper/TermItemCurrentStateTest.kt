package com.gdavidpb.tuindice.record.presentation.mapper

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
	fun isAttemptReadOnly_returnsTrue_forHistoricalTerms() {
		val term = termProjection(kind = TermKind.OFFICIAL_HISTORICAL)

		assertTrue(term.isAttemptReadOnly())
	}

	@Test
	fun isAttemptReadOnly_returnsFalse_forEditableTermKinds() {
		assertFalse(termProjection(kind = TermKind.OFFICIAL_CURRENT).isAttemptReadOnly())
		assertFalse(termProjection(kind = TermKind.SYNTHETIC).isAttemptReadOnly())
	}
}

private fun termProjection(
	kind: TermKind
) = TermProjection(
	id = "term-id",
	label = "Term",
	startAtMillis = 1_000L,
	endAtMillis = 2_000L,
	kind = kind,
	periodAverage = 0.0,
	cumulativeAverage = 0.0,
	periodCredits = 0,
	cumulativeCredits = 0,
	attempts = emptyList()
)
