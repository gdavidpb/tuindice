package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordStatisticsEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SummaryTermReportBuilderTest {

	// A closed Jan - Mar with two passed subjects of 3 credits each, and the Apr - Jul term in progress with a passed, a failed,
	// a pending and a withdrawn subject.
	private val closedJanMar = hubClosedTerm(
		year = 2026,
		attempts = listOf(hubPassedAttempt("CI1"), hubPassedAttempt("CI2")),
		index = 4.0,
		periodCode = AcademicTermPeriod.JAN_MAR
	)

	private val currentAprJul = hubTerm(
		kind = TermKind.CURRENT,
		periodCode = AcademicTermPeriod.APR_JUL,
		attempts = listOf(
			hubPassedAttempt("CI3").copy(academicScore = AttemptScore.numeric(5)),
			hubAttempt("CI4").copy(academicScore = AttemptScore.numeric(2), academicOutcome = AttemptOutcome.FAILED),
			hubAttempt("CI6"),
			hubAttempt("EG5", withdrawn = true)
		)
	)

	private val record = hubRecord(closedJanMar, currentAprJul)
	private val projection = RecordProjectionEngine.projectAcademic(record)
	private val closed = RecordStatisticsEngine.statisticsOf(projection)

	@Test
	fun theFiguresOfTheTermAreTheEnginesOwn() {
		val report = assertNotNull(termReportOf(termReportInsight(termKey = "2026-APR_JUL"), projection, closed))

		val engineIndex = projection.terms.first { term -> term.termKey == "2026-APR_JUL" }.periodAverage
		assertEquals(engineIndex, report.term.index)
		assertEquals(3, report.term.approvedCredits)
	}

	@Test
	fun theComparisonIsWithThePreviousTermOfTheRecord() {
		val report = assertNotNull(termReportOf(termReportInsight(termKey = "2026-APR_JUL"), projection, closed))

		assertEquals("2026-JAN_MAR", report.previousTerm?.termKey)
		assertEquals(report.term.index?.minus(4.0), report.indexDelta)
	}

	@Test
	fun theSubjectsAreSortedByCodeAndSayWhatTheTermSaysOfThem() {
		val report = assertNotNull(termReportOf(termReportInsight(termKey = "2026-APR_JUL"), projection, closed))

		assertEquals(listOf("CI3", "CI4", "CI6", "EG5"), report.subjects.map { subject -> subject.subjectCode })
		assertEquals(
			listOf(
				SummaryTermReportOutcome.APPROVED,
				SummaryTermReportOutcome.FAILED,
				SummaryTermReportOutcome.PENDING,
				SummaryTermReportOutcome.WITHDRAWN
			),
			report.subjects.map { subject -> subject.outcome }
		)
		assertEquals(2, report.subjects[1].numericGrade)
	}

	@Test
	fun theCreditsOfTheTermLeaveOutTheWithdrawnSubjects() {
		val report = assertNotNull(termReportOf(termReportInsight(termKey = "2026-APR_JUL"), projection, closed))

		assertEquals(9, report.creditsOfTerm)
	}

	@Test
	fun thePaceCountsTheRegularTermsIncludingTheTermInProgress() {
		val report = assertNotNull(termReportOf(termReportInsight(termKey = "2026-APR_JUL"), projection, closed))

		assertEquals(listOf("2026-JAN_MAR", "2026-APR_JUL"), report.paceTerms.map { term -> term.termKey })
		// Six approved credits in the closed term and three in this one: the average of the two regular terms.
		assertEquals(4.5, report.averageCredits)
	}

	@Test
	fun aTermThatTheRecordDoesNotHoldIsNoReport() {
		val report = termReportOf(termReportInsight(termKey = "2027-JAN_MAR"), projection, closed)

		assertNull(report)
	}

	@Test
	fun theJulyAugustTermIsLeftOutOfThePace() {
		val summer = hubTerm(
			kind = TermKind.CURRENT,
			periodCode = AcademicTermPeriod.JUL_AUG,
			attempts = listOf(hubPassedAttempt("CI7"))
		)
		val summerProjection = RecordProjectionEngine.projectAcademic(hubRecord(closedJanMar, summer))
		val summerClosed = RecordStatisticsEngine.statisticsOf(summerProjection)

		val report = assertNotNull(
			termReportOf(termReportInsight(termKey = "2026-JUL_AUG"), summerProjection, summerClosed)
		)

		assertEquals(listOf("2026-JAN_MAR"), report.paceTerms.map { term -> term.termKey })
		assertEquals(false, report.term.isRegular)
	}
}
