package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
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
	fun isCurrentTerm_returnsTrue_forCurrentTerms() {
		val term = termProjection(
			kind = TermKind.CURRENT
		)

		assertTrue(term.isCurrentTerm())
	}

	@Test
	fun isCurrentTerm_returnsFalse_forNonCurrentKinds() {
		val term = termProjection(
			kind = TermKind.HISTORICAL
		)

		assertFalse(term.isCurrentTerm())
	}

	@Test
	fun canDeleteTerm_returnsTrue_onlyForSyntheticTerms() {
		assertTrue(termProjection(kind = TermKind.SYNTHETIC).canDeleteTerm())
		assertFalse(termProjection(kind = TermKind.CURRENT).canDeleteTerm())
		assertFalse(termProjection(kind = TermKind.HISTORICAL).canDeleteTerm())
	}

	@Test
	fun canEditTerm_returnsTrue_onlyForSyntheticTermsInProjectionMode() {
		assertTrue(termProjection(kind = TermKind.SYNTHETIC).canEditTerm(RecordViewMode.Projection))
		assertFalse(termProjection(kind = TermKind.SYNTHETIC).canEditTerm(RecordViewMode.Historical))
		assertFalse(termProjection(kind = TermKind.CURRENT).canEditTerm(RecordViewMode.Projection))
		assertFalse(termProjection(kind = TermKind.HISTORICAL).canEditTerm(RecordViewMode.Projection))
	}

	@Test
	fun isAttemptReadOnly_returnsTrue_forHistoricalTerms() {
		val term = termProjection(kind = TermKind.HISTORICAL)

		assertTrue(term.isAttemptReadOnly(RecordViewMode.Projection))
	}

	@Test
	fun isAttemptReadOnly_returnsTrue_forAnyTermInHistoricalMode() {
		assertTrue(termProjection(kind = TermKind.CURRENT).isAttemptReadOnly(RecordViewMode.Historical))
		assertTrue(termProjection(kind = TermKind.SYNTHETIC).isAttemptReadOnly(RecordViewMode.Historical))
	}

	@Test
	fun isAttemptReadOnly_returnsFalse_forEditableTermKinds_inProjectionMode() {
		assertFalse(termProjection(kind = TermKind.CURRENT).isAttemptReadOnly(RecordViewMode.Projection))
		assertFalse(termProjection(kind = TermKind.SYNTHETIC).isAttemptReadOnly(RecordViewMode.Projection))
	}

	@Test
	fun filterByViewMode_returnsOnlyHistoricalTerms_inHistoricalMode() {
		val visibleTerms = listOf(
			termProjection(id = "historical", kind = TermKind.HISTORICAL),
			termProjection(id = "current", kind = TermKind.CURRENT),
			termProjection(id = "synthetic", kind = TermKind.SYNTHETIC)
		).filterByViewMode(RecordViewMode.Historical)

		assertEquals(listOf("historical"), visibleTerms.map(TermProjection::id))
	}

	@Test
	fun filterByViewMode_keepsAllTerms_inProjectionMode() {
		val visibleTerms = listOf(
			termProjection(id = "historical", kind = TermKind.HISTORICAL),
			termProjection(id = "current", kind = TermKind.CURRENT),
			termProjection(id = "synthetic", kind = TermKind.SYNTHETIC)
		).filterByViewMode(RecordViewMode.Projection)

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
	periodYear = 2026,
	periodCode = AcademicTermPeriod.JAN_MAR,
	termKey = "2026-JAN_MAR",
	termOrder = 20261,
	periodLabel = "Enero - Marzo 2026",
	kind = kind,
	periodAverage = 0.0,
	cumulativeAverage = 0.0,
	periodCredits = 0,
	cumulativeCredits = 0,
	attempts = emptyList()
)
