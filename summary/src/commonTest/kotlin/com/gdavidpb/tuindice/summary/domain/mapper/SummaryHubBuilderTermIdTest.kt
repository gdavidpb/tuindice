package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryHubBuilderTermIdTest {
	private fun hubOf(vararg terms: AcademicTerm) = buildSummaryHub(
		recordState = SummaryRecordState.Ready(hubRecord(*terms)),
		evaluations = ObservedSyncedSnapshot(value = emptyList(), hasSynced = true),
		now = scheduleNow(),
		pensumProgress = null
	)

	@Test
	fun withATermInProgress_theTermCarriesTheIdTheRecordGivesIt() {
		val current = hubTerm(attempts = listOf(hubAttempt(code = "CI5437")))

		val term = assertIs<SummaryTerm.InProgress>(hubOf(current).term)

		assertEquals(current.id, term.termId)
	}

	@Test
	fun withTermsOverToo_theTermInProgressCarriesItsOwnIdNotTheirs() {
		val closed = hubTerm(
			attempts = emptyList(),
			kind = TermKind.HISTORICAL,
			periodCode = AcademicTermPeriod.JUL_AUG
		)
		val current = hubTerm(attempts = listOf(hubAttempt(code = "CI5437")))

		val term = assertIs<SummaryTerm.InProgress>(hubOf(closed, current).term)

		assertEquals(current.id, term.termId)
	}

	@Test
	fun withNoTermInProgress_thereIsNoTermToCarryAnId() {
		val closed = hubTerm(attempts = emptyList(), kind = TermKind.HISTORICAL)

		assertIs<SummaryTerm.Idle>(hubOf(closed).term)
	}
}
