package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.summary.domain.model.SummaryAttention
import com.gdavidpb.tuindice.summary.domain.model.SummaryCurrentTermStatistics
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryRecordState
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubDate
import com.gdavidpb.tuindice.summary.testing.hubEvaluation
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.testing.scheduleNow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SummaryCurrentTermStatisticsTest {
	private val today = hubDate(15)

	// The hub of a ready record whose term in progress has these subjects and evaluations, read on the day given.
	private fun hubOf(
		attempts: List<AcademicAttempt> = listOf(hubAttempt(code = "CI5437")),
		evaluations: List<Evaluation> = emptyList(),
		hasSynced: Boolean = true,
		day: LocalDate = today
	): SummaryHub {
		return buildSummaryHub(
			recordState = SummaryRecordState.Ready(hubRecord(hubTerm(attempts = attempts))),
			evaluations = ObservedSyncedSnapshot(value = evaluations, hasSynced = hasSynced),
			now = scheduleNow(date = day),
			pensumProgress = null
		)
	}

	private fun currentOf(
		attempts: List<AcademicAttempt> = listOf(hubAttempt(code = "CI5437")),
		evaluations: List<Evaluation> = emptyList(),
		hasSynced: Boolean = true,
		day: LocalDate = today
	): SummaryCurrentTermStatistics {
		return assertNotNull(hubOf(attempts, evaluations, hasSynced, day).statistics?.currentTerm)
	}

	@Test
	fun aTermInProgress_hasACurrentTerm_andAHistoricalRecordHasNone() {
		val current = currentOf()

		assertEquals(AcademicTermPeriod.SEP_DEC, current.periodCode)
		assertEquals(2026, current.periodYear)

		val finished = buildSummaryHub(
			recordState = SummaryRecordState.Ready(
				hubRecord(hubTerm(attempts = listOf(hubAttempt(code = "CI5437")), kind = TermKind.HISTORICAL))
			),
			evaluations = ObservedSyncedSnapshot(value = emptyList(), hasSynced = true),
			now = scheduleNow(),
			pensumProgress = null
		)

		assertNull(finished.statistics?.currentTerm)
	}

	@Test
	fun theEvaluationTypes_areThoseTheTermHas_inTheOrderOfTheTypes_andWeightedByTheirMaximums() {
		val current = currentOf(
			evaluations = listOf(
				hubEvaluation(id = "essay", code = "CI5437", date = hubDate(5), type = EvaluationType.ESSAY),
				// Four out of five, and ten out of twenty: 14 of 25 points, not the mean of 80 % and 50 %.
				hubEvaluation(id = "test-1", code = "CI5437", date = hubDate(1), grade = 4.0).copy(maxGrade = 5.0),
				hubEvaluation(id = "test-2", code = "CI5437", date = hubDate(3), grade = 10.0),
				hubEvaluation(id = "test-3", code = "CI5437", date = hubDate(30))
			)
		)

		assertEquals(listOf(EvaluationType.TEST, EvaluationType.ESSAY), current.evaluationTypes.map { it.type })
		assertTrue(current.evaluationTypes.none { it.type == EvaluationType.QUIZ })

		val tests = current.evaluationTypes.first()
		assertEquals(14.0, tests.earned)
		assertEquals(25.0, tests.evaluated)
		assertEquals(2, tests.gradedCount)
		assertEquals(3, tests.totalCount)
		assertEquals(14.0 / 25.0, tests.performance)

		val essays = current.evaluationTypes.last()
		assertEquals(0, essays.gradedCount)
		assertEquals(1, essays.totalCount)
		assertNull(essays.performance)
	}

	@Test
	fun theEvaluatedFraction_countsEachSubjectOverItsOneHundredPoints_andCapsEachAtOneHundred() {
		val full = currentOf(
			evaluations = (1..6).map { day -> hubEvaluation(id = "g$day", code = "CI5437", date = hubDate(day), grade = 10.0) }
		)
		// Six graded evaluations of twenty points are 120 evaluated: the subject counts as its hundred, and no more.
		assertEquals(1.0, full.evaluatedFraction)

		val partial = currentOf(
			attempts = listOf(hubAttempt(code = "CI5437"), hubAttempt(code = "MA1111")),
			evaluations = (1..3).map { day -> hubEvaluation(id = "c$day", code = "CI5437", date = hubDate(day), grade = 10.0) }
		)
		// Sixty evaluated out of two subjects of one hundred each.
		assertEquals(60.0 / 200.0, partial.evaluatedFraction)
	}

	@Test
	fun withNoSubjectToEvaluate_theFractionIsNotKnown() {
		val current = currentOf(attempts = listOf(hubAttempt(code = "CI5437", withdrawn = true)))

		assertNull(current.evaluatedFraction)
	}

	@Test
	fun withSyncedEvaluationsButNoneRegistered_theFractionIsZero_andNotUnknown() {
		assertEquals(0.0, currentOf().evaluatedFraction)
	}

	@Test
	fun theOverdueWithoutGrade_countsTheEvaluationsThatWentByWithoutAGrade_onTheDayItIsRead() {
		val evaluations = listOf(
			hubEvaluation(id = "past", code = "CI5437", date = hubDate(14)),
			hubEvaluation(id = "today", code = "CI5437", date = today),
			hubEvaluation(id = "graded", code = "CI5437", date = hubDate(1), grade = 12.0),
			hubEvaluation(id = "future", code = "CI5437", date = hubDate(30))
		)

		assertEquals(1, currentOf(evaluations = evaluations).overdueWithoutGrade)
		// Tomorrow the one of today has gone by too.
		assertEquals(2, currentOf(evaluations = evaluations, day = hubDate(16)).overdueWithoutGrade)
	}

	@Test
	fun theOverdueWithoutGrade_isTheCountTheAttentionOfTheHubSays() {
		val hub = hubOf(
			evaluations = listOf(hubEvaluation(id = "past", code = "CI5437", date = hubDate(14)))
		)

		val attention = hub.attention.filterIsInstance<SummaryAttention.UngradedEvaluations>().single()

		assertEquals(attention.count, hub.statistics?.currentTerm?.overdueWithoutGrade)
	}

	@Test
	fun withTheEvaluationsNotDownloaded_nothingIsKnown_evenIfSomeAreListed() {
		val current = currentOf(
			evaluations = listOf(
				hubEvaluation(id = "past", code = "CI5437", date = hubDate(14)),
				hubEvaluation(id = "graded", code = "CI5437", date = hubDate(1), grade = 12.0)
			),
			hasSynced = false
		)

		assertFalse(current.hasSyncedEvaluations)
		assertTrue(current.evaluationTypes.isEmpty())
		assertEquals(0, current.overdueWithoutGrade)
		assertNull(current.evaluatedFraction)
	}
}
