package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.testing.academicTerm
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectedTermFlagsTest {
	@Test
	fun isCurrentTerm_isTrueForTheCurrentTermOnly() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "past", kind = TermKind.HISTORICAL, periodYear = 2025),
				academicTerm(id = "now", kind = TermKind.CURRENT, periodYear = 2026)
			)
		)

		assertTrue(record.isCurrentTerm(termId = "now"))
		assertFalse(record.isCurrentTerm(termId = "past"))
	}

	@Test
	fun isCurrentTerm_withSeveralCurrentTerms_isTrueForTheOneWithTheHighestOrder() {
		// The earlier term is listed first, so only the official rule, not the list order, picks the later one.
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "earlier", kind = TermKind.CURRENT, periodYear = 2025),
				academicTerm(id = "later", kind = TermKind.CURRENT, periodYear = 2026)
			)
		)

		assertTrue(record.isCurrentTerm(termId = "later"))
		assertFalse(record.isCurrentTerm(termId = "earlier"))
	}

	@Test
	fun isCurrentTerm_isFalseWhenNoTermIsCurrent() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(academicTerm(id = "past", kind = TermKind.HISTORICAL))
		)

		assertFalse(record.isCurrentTerm(termId = "past"))
	}

	@Test
	fun isCurrentTerm_isFalseForATermTheRecordDoesNotHave() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(academicTerm(id = "now", kind = TermKind.CURRENT))
		)

		assertFalse(record.isCurrentTerm(termId = "unknown"))
	}

	@Test
	fun isSyntheticTerm_isTrueForTheSyntheticTermOnly() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "now", kind = TermKind.CURRENT),
				academicTerm(id = "made-by-me", kind = TermKind.SYNTHETIC)
			)
		)

		assertTrue(record.isSyntheticTerm(termId = "made-by-me"))
		assertFalse(record.isSyntheticTerm(termId = "now"))
		assertFalse(record.isSyntheticTerm(termId = "unknown"))
	}
}
