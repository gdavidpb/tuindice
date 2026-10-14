package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryHubStatisticsTest {
	private val progress = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)

	private fun hubOf(
		recordState: SummaryRecordState,
		pensumProgress: PensumCreditsProgress? = progress
	): SummaryHub {
		return buildSummaryHub(
			recordState = recordState,
			evaluations = ObservedSyncedSnapshot(value = emptyList(), hasSynced = true),
			now = scheduleNow(),
			pensumProgress = pensumProgress
		)
	}

	@Test
	fun aReadyRecordWithATermInProgress_hasStatisticsWithItsCurrentTerm_andTheClosedTermsBeforeIt() {
		val closed = hubClosedTerm(year = 2025, attempts = listOf(hubPassedAttempt(code = "MA1111")), index = 13.5)
		val record = hubRecord(closed, hubTerm(attempts = listOf(hubAttempt(code = "CI5437"))))

		val statistics = assertNotNull(hubOf(SummaryRecordState.Ready(record)).statistics)

		assertEquals(1, statistics.closed.terms.size)
		assertEquals(2025, statistics.lastClosedTerm?.periodYear)
		assertNotNull(statistics.currentTerm)
	}

	@Test
	fun aReadyRecordWithOnlyClosedTerms_hasNoCurrentTerm_andTheBestAndWorstOfThem_andItsPace() {
		val record = hubRecord(
			hubClosedTerm(year = 2023, attempts = listOf(hubPassedAttempt(code = "A1")), index = 14.0),
			hubClosedTerm(year = 2024, attempts = listOf(hubPassedAttempt(code = "B1")), index = 12.0)
		)

		val statistics = assertNotNull(hubOf(SummaryRecordState.Ready(record)).statistics)

		assertNull(statistics.currentTerm)
		assertEquals(2023, statistics.bestTerm?.periodYear)
		assertEquals(2024, statistics.worstTerm?.periodYear)
		assertEquals(2024, statistics.lastClosedTerm?.periodYear)
		assertEquals(progress, statistics.pensumProgress)
		// Three credits a regular term, and the 84 credits the pensum has left: 28 terms at that pace.
		assertEquals(3.0, statistics.pace.averageCredits)
		assertEquals(28, statistics.pace.remainingTerms)
	}

	@Test
	fun aNewStudentWithoutRecord_hasEmptyStatistics_withThePensumAsItWasRead() {
		val statistics = assertNotNull(hubOf(SummaryRecordState.NewStudentWithoutRecord).statistics)

		assertTrue(statistics.closed.terms.isEmpty())
		assertEquals(0, statistics.closed.subjects.approved)
		assertNull(statistics.pace.averageCredits)
		assertNull(statistics.pace.remainingTerms)
		assertNull(statistics.bestTerm)
		assertNull(statistics.worstTerm)
		assertNull(statistics.lastClosedTerm)
		assertNull(statistics.currentTerm)
		assertEquals(progress, statistics.pensumProgress)
	}

	@Test
	fun aRecordThatIsNotReady_hasNoStatistics() {
		assertNull(hubOf(SummaryRecordState.NotReady).statistics)
	}

	@Test
	fun theFinishedTerm_andTheLastClosedTerm_areOneTerm_becauseTheyAreReadFromOneProjection() {
		val record = hubRecord(
			hubClosedTerm(year = 2024, attempts = listOf(hubPassedAttempt(code = "A1")), index = 13.5),
			hubClosedTerm(
				year = 2025,
				attempts = listOf(hubPassedAttempt(code = "B1"), hubPassedAttempt(code = "B2")),
				index = 12.0
			)
		)

		val hub = hubOf(SummaryRecordState.Ready(record))
		val finished = assertNotNull(assertIs<SummaryTerm.Idle>(hub.term).finished)
		val last = assertNotNull(assertNotNull(hub.statistics).lastClosedTerm)

		assertEquals(last.periodCode, finished.periodCode)
		assertEquals(last.periodYear, finished.periodYear)
		assertEquals(last.index ?: 0.0, finished.index)
		assertEquals(last.approvedCredits, finished.approvedCredits)
	}
}
