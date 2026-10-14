package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordStatisticsEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubQualitativeAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SummaryStatisticsBuilderTest {
	private val pensum = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)

	// The statistics of a record whose terms are all closed: there is no term in progress.
	private fun statisticsOf(
		vararg terms: AcademicTerm,
		pensumProgress: PensumCreditsProgress? = null
	): SummaryStatistics {
		return summaryStatisticsOf(
			closed = RecordStatisticsEngine.statisticsOf(RecordProjectionEngine.projectAcademic(hubRecord(*terms))),
			pensumProgress = pensumProgress,
			currentTerm = null
		)
	}

	// A regular term whose four subjects were passed: twelve credits in all, and the index given.
	private fun regularTerm(year: Int, prefix: String, index: Double? = 13.0): AcademicTerm {
		return hubClosedTerm(
			year = year,
			attempts = (1..4).map { number -> hubPassedAttempt(code = "$prefix$number") },
			index = index
		)
	}

	/* The best and the worst closed term, and the last one that closed */

	@Test
	fun theBestTerm_isTheHighestIndex_andOnATie_theMostRecentOfThem() {
		val summary = statisticsOf(
			regularTerm(year = 2023, prefix = "A", index = 14.0),
			hubClosedTerm(year = 2024, attempts = listOf(hubQualitativeAttempt(code = "C1"))),
			regularTerm(year = 2025, prefix = "B", index = 14.0)
		)

		assertEquals(2025, summary.bestTerm?.periodYear)
	}

	@Test
	fun theWorstTerm_isTheLowestIndex_andOnATie_theMostRecentOfThem() {
		val summary = statisticsOf(
			regularTerm(year = 2022, prefix = "A", index = 12.0),
			regularTerm(year = 2023, prefix = "B", index = 14.0),
			hubClosedTerm(year = 2024, attempts = listOf(hubQualitativeAttempt(code = "C1"))),
			regularTerm(year = 2025, prefix = "D", index = 12.0)
		)

		assertEquals(2025, summary.worstTerm?.periodYear)
		assertEquals(2023, summary.bestTerm?.periodYear)
	}

	@Test
	fun aTermWithNoIndex_isNeitherTheBestNorTheWorst_butItIsTheLastOneClosed() {
		val summary = statisticsOf(
			regularTerm(year = 2023, prefix = "A", index = 12.0),
			hubClosedTerm(year = 2024, attempts = listOf(hubQualitativeAttempt(code = "B1")))
		)

		assertEquals(2023, summary.bestTerm?.periodYear)
		assertEquals(2023, summary.worstTerm?.periodYear)
		assertEquals(2024, summary.lastClosedTerm?.periodYear)
		assertNull(summary.lastClosedTerm?.index)
	}

	@Test
	fun theJulyAugustTerm_isNeitherTheBestNorTheWorst_whateverItsIndex() {
		val summary = statisticsOf(
			regularTerm(year = 2023, prefix = "A", index = 12.0),
			hubClosedTerm(
				year = 2023,
				attempts = listOf(hubPassedAttempt(code = "B1")),
				index = 20.0,
				periodCode = AcademicTermPeriod.JUL_AUG
			),
			regularTerm(year = 2024, prefix = "C", index = 14.0),
			hubClosedTerm(
				year = 2024,
				attempts = listOf(hubPassedAttempt(code = "D1")),
				index = 6.0,
				periodCode = AcademicTermPeriod.JUL_AUG
			)
		)

		assertEquals(AcademicTermPeriod.SEP_DEC, summary.bestTerm?.periodCode)
		assertEquals(2024, summary.bestTerm?.periodYear)
		assertEquals(2023, summary.worstTerm?.periodYear)
		assertEquals(AcademicTermPeriod.SEP_DEC, summary.worstTerm?.periodCode)
	}

	@Test
	fun withNoClosedTerm_thereIsNoBestWorstOrLastOne() {
		val summary = statisticsOf()

		assertNull(summary.bestTerm)
		assertNull(summary.worstTerm)
		assertNull(summary.lastClosedTerm)
	}

	/* The pace */

	@Test
	fun thePace_isTheApprovedCreditsOfTheRegularTerms_andTheTermsThePensumStillTakes() {
		val summary = statisticsOf(
			regularTerm(year = 2023, prefix = "A"),
			regularTerm(year = 2024, prefix = "B"),
			regularTerm(year = 2025, prefix = "C"),
			pensumProgress = pensum
		)

		assertEquals(12.0, summary.pace.averageCredits)
		// The pensum has 84 credits left, and at twelve a term that is seven terms.
		assertEquals(7, summary.pace.remainingTerms)
		assertEquals(pensum, summary.pensumProgress)
	}

	@Test
	fun withoutThePensum_thePaceHasItsAverage_butNoTermsToProject() {
		val summary = statisticsOf(
			regularTerm(year = 2023, prefix = "A"),
			regularTerm(year = 2024, prefix = "B"),
			regularTerm(year = 2025, prefix = "C")
		)

		assertEquals(12.0, summary.pace.averageCredits)
		assertNull(summary.pace.remainingTerms)
		assertNull(summary.pensumProgress)
	}

	@Test
	fun withOneRegularTerm_thereIsNoTermToProjectYet() {
		val summary = statisticsOf(regularTerm(year = 2025, prefix = "A"), pensumProgress = pensum)

		assertEquals(12.0, summary.pace.averageCredits)
		assertNull(summary.pace.remainingTerms)
	}

	@Test
	fun theJulyAugustTerm_isShown_butItDoesNotCountTowardThePace() {
		val summary = statisticsOf(
			regularTerm(year = 2024, prefix = "A"),
			regularTerm(year = 2025, prefix = "B"),
			hubClosedTerm(
				year = 2025,
				attempts = listOf(hubPassedAttempt(code = "C1"), hubPassedAttempt(code = "C2")),
				index = 13.0,
				periodCode = AcademicTermPeriod.JUL_AUG
			),
			pensumProgress = pensum
		)

		assertFalse(summary.closed.terms.single { term -> term.periodCode == AcademicTermPeriod.JUL_AUG }.isRegular)
		assertEquals(12.0, summary.pace.averageCredits)
		assertEquals(7, summary.pace.remainingTerms)
	}

	@Test
	fun aRegularTermWithNoIndex_stillCountsTheCreditsItApproved_towardThePace() {
		val summary = statisticsOf(
			regularTerm(year = 2023, prefix = "A"),
			hubClosedTerm(
				year = 2024,
				attempts = listOf(hubQualitativeAttempt(code = "B1"), hubQualitativeAttempt(code = "B2"))
			),
			regularTerm(year = 2025, prefix = "C"),
			pensumProgress = pensum
		)

		assertNull(summary.closed.terms.single { term -> term.periodYear == 2024 }.index)
		// Twelve, six and twelve credits over three regular terms: ten a term, so 84 credits take nine terms.
		assertEquals(10.0, summary.pace.averageCredits)
		assertEquals(9, summary.pace.remainingTerms)
	}
}
