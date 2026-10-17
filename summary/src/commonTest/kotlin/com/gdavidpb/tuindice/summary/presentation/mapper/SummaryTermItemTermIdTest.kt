package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SummaryTermItemTermIdTest {
	@Test
	fun withATermInProgress_theItemCarriesTheIdOfThatTerm() {
		val term = SummaryTerm.InProgress(
			periodCode = AcademicTermPeriod.SEP_DEC,
			periodYear = 2026,
			academicWeek = 3,
			subjects = emptyList(),
			hasSyncedEvaluations = true,
			termId = "term-2026-SEP_DEC"
		)

		assertEquals("term-2026-SEP_DEC", term.toSummaryTermItem().termId)
	}

	@Test
	fun withNoTermInProgress_theItemCarriesNoId() {
		val term = SummaryTerm.Idle(hasTerms = true, finished = null)

		assertNull(term.toSummaryTermItem().termId)
	}
}
