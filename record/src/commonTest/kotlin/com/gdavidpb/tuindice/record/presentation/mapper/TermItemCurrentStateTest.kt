package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.filterByViewMode
import kotlin.test.Test
import kotlin.test.assertEquals
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

		assertTrue(term.isAttemptReadOnly(RecordViewMode.Working))
	}

	@Test
	fun isAttemptReadOnly_returnsTrue_forAnyTermInOfficialMode() {
		assertTrue(termProjection(kind = TermKind.OFFICIAL_CURRENT).isAttemptReadOnly(RecordViewMode.Official))
		assertTrue(termProjection(kind = TermKind.SYNTHETIC).isAttemptReadOnly(RecordViewMode.Official))
	}

	@Test
	fun isAttemptReadOnly_returnsFalse_forEditableTermKinds_inWorkingMode() {
		assertFalse(termProjection(kind = TermKind.OFFICIAL_CURRENT).isAttemptReadOnly(RecordViewMode.Working))
		assertFalse(termProjection(kind = TermKind.SYNTHETIC).isAttemptReadOnly(RecordViewMode.Working))
	}

	@Test
	fun filterByViewMode_returnsOnlyHistoricalTerms_inOfficialMode() {
		val visibleTerms = listOf(
			termProjection(id = "historical", kind = TermKind.OFFICIAL_HISTORICAL),
			termProjection(id = "current", kind = TermKind.OFFICIAL_CURRENT),
			termProjection(id = "synthetic", kind = TermKind.SYNTHETIC)
		).filterByViewMode(RecordViewMode.Official)

		assertEquals(listOf("historical"), visibleTerms.map(TermProjection::id))
	}

	@Test
	fun filterByViewMode_keepsAllTerms_inWorkingMode() {
		val visibleTerms = listOf(
			termProjection(id = "historical", kind = TermKind.OFFICIAL_HISTORICAL),
			termProjection(id = "current", kind = TermKind.OFFICIAL_CURRENT),
			termProjection(id = "synthetic", kind = TermKind.SYNTHETIC)
		).filterByViewMode(RecordViewMode.Working)

		assertEquals(
			listOf("historical", "current", "synthetic"),
			visibleTerms.map(TermProjection::id)
		)
	}
}

private fun termProjection(
	id: String = "term-id",
	kind: TermKind
) = TermProjection(
	id = id,
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
