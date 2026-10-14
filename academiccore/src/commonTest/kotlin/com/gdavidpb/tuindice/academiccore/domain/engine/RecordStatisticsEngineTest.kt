package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.CreditOutcomes
import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.RecordStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecordStatisticsEngineTest {
	@Test
	fun retakeAfterAFailure_isApproved_butNotAtFirstTry_andIsRepeated() {
		val stats = statisticsOf(
			term(
				id = "t1",
				year = 2023,
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
			),
			term(
				id = "t2",
				year = 2024,
				attempts = listOf(numeric(id = "a2", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED))
			)
		)

		assertEquals(1, stats.subjects.approved)
		assertEquals(0, stats.subjects.failed)
		assertEquals(0, stats.subjects.approvedFirstTry)
		assertEquals(1, stats.subjects.firstTryBase)
		assertEquals(1, stats.subjects.repeated)
		assertEquals(3, stats.credits.approved)
		assertEquals(listOf(2.0, 4.0), stats.terms.map(TermStatistic::index))
		assertEquals(listOf(2.0, 4.0), stats.terms.map(TermStatistic::cumulativeIndex))
		assertEquals(listOf(0, 1), stats.terms.map(TermStatistic::approvedSubjects))
		assertEquals(mapOf(1 to 0, 2 to 1, 3 to 0, 4 to 1, 5 to 0), stats.grades.countsByGrade)
	}

	@Test
	fun firstTimeApproval_isApprovedAtFirstTry_andIsNotRepeated() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED))
			)
		)

		assertEquals(1, stats.subjects.approved)
		assertEquals(1, stats.subjects.approvedFirstTry)
		assertEquals(1, stats.subjects.firstTryBase)
		assertEquals(0, stats.subjects.repeated)
	}

	@Test
	fun approvalFollowedByAFailure_staysApproved_andWasApprovedAtFirstTry() {
		val stats = statisticsOf(
			term(
				id = "t1",
				year = 2023,
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED))
			),
			term(
				id = "t2",
				year = 2024,
				attempts = listOf(numeric(id = "a2", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
			)
		)

		assertEquals(1, stats.subjects.approved)
		assertEquals(0, stats.subjects.failed)
		assertEquals(1, stats.subjects.approvedFirstTry)
		assertEquals(1, stats.subjects.repeated)
	}

	@Test
	fun failureFollowedByAWithdrawal_staysFailed() {
		val stats = statisticsOf(
			term(
				id = "t1",
				year = 2023,
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
			),
			term(
				id = "t2",
				year = 2024,
				attempts = listOf(numeric(id = "a2", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.RETIRED))
			)
		)

		assertEquals(1, stats.subjects.failed)
		assertEquals(0, stats.subjects.withdrawn)
		assertEquals(3, stats.credits.failed)
		assertEquals(0, stats.subjects.repeated)
	}

	@Test
	fun withdrawnFlag_makesTheSubjectWithdrawn_evenWithAFailedGrade() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED)
						.copy(withdrawn = true)
				)
			)
		)

		assertEquals(1, stats.subjects.withdrawn)
		assertEquals(0, stats.subjects.failed)
		assertEquals(0, stats.subjects.noResult)
		assertEquals(3, stats.credits.withdrawn)
		assertEquals(0, stats.credits.failed)
		assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), stats.grades.countsByGrade)
	}

	@Test
	fun withdrawnFlag_isNotAnApproval_evenWithAnApprovedOutcome() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED)
						.copy(withdrawn = true)
				)
			)
		)

		assertEquals(1, stats.subjects.withdrawn)
		assertEquals(0, stats.subjects.approved)
		assertEquals(0, stats.terms.single().approvedCredits)
		assertEquals(0, stats.terms.single().approvedSubjects)
	}

	@Test
	fun retiredOutcome_isWithdrawn_andLeavesTheTermWithoutIndex() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(numeric(id = "a1", code = "CI2525", credits = 3, value = 2, outcome = AttemptOutcome.RETIRED))
			)
		)

		assertEquals(1, stats.subjects.withdrawn)
		assertEquals(3, stats.credits.withdrawn)
		assertNull(stats.terms.single().index)
		assertNull(stats.terms.single().cumulativeIndex)
	}

	@Test
	fun unreportedGrade_isNoResult_andOnlyTheTallyCountsIt() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(unreported(id = "a1", code = "MA1111", credits = 3))
			)
		)

		assertEquals(1, stats.subjects.noResult)
		assertEquals(0, stats.subjects.approved)
		assertEquals(0, stats.subjects.failed)
		assertEquals(1, stats.grades.unreported)
		assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), stats.grades.countsByGrade)
	}

	@Test
	fun unreportedAttempt_isNotAResult_soTheApprovalAfterItIsTheFirstTry() {
		val stats = statisticsOf(
			term(
				id = "t1",
				year = 2023,
				attempts = listOf(unreported(id = "a1", code = "MA1111", credits = 3))
			),
			term(
				id = "t2",
				year = 2024,
				attempts = listOf(numeric(id = "a2", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED))
			)
		)

		assertEquals(1, stats.subjects.approved)
		assertEquals(0, stats.subjects.noResult)
		assertEquals(1, stats.subjects.approvedFirstTry)
		assertEquals(1, stats.grades.unreported)
	}

	@Test
	fun qualitativeApproval_countsAsApprovedSubjectAndCredits_andNotInTheHistogram() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(qualitative(id = "a1", code = "ID1101", credits = 2, symbol = "A"))
			)
		)

		assertEquals(1, stats.subjects.approved)
		assertEquals(1, stats.subjects.approvedFirstTry)
		assertEquals(2, stats.credits.approved)
		assertEquals(2, stats.terms.single().approvedCredits)
		assertEquals(1, stats.terms.single().approvedSubjects)
		assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), stats.grades.countsByGrade)
		assertNull(stats.terms.single().index)
	}

	@Test
	fun qualitativeWithdrawal_isWithdrawn_andNotInTheHistogram() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(qualitative(id = "a1", code = "ID1101", credits = 2, symbol = "R"))
			)
		)

		assertEquals(1, stats.subjects.withdrawn)
		assertEquals(2, stats.credits.withdrawn)
		assertEquals(0, stats.subjects.approved)
		assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), stats.grades.countsByGrade)
	}

	@Test
	fun zeroGrade_isNoGrade_soAFailureWithItIsNoResult() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 0, outcome = AttemptOutcome.FAILED))
			)
		)

		assertEquals(1, stats.subjects.noResult)
		assertEquals(0, stats.subjects.failed)
		assertEquals(0, stats.credits.failed)
		assertEquals(0, stats.grades.unreported)
		assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), stats.grades.countsByGrade)
		assertNull(stats.terms.single().index)
	}

	@Test
	fun termWithoutAnyCountingAttempt_hasNoIndex() {
		val stats = statisticsOf(term(id = "t1"))

		val closed = stats.terms.single()
		assertNull(closed.index)
		assertNull(closed.cumulativeIndex)
		assertEquals(0, closed.approvedSubjects)
	}

	@Test
	fun unreportedOnlyTerm_hasNoIndex() {
		// The projection counts the unreported attempt with no grade, so its period average is 0.0: no grade to show.
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(unreported(id = "a1", code = "MA1111", credits = 3))
			)
		)

		assertNull(stats.terms.single().index)
	}

	@Test
	fun officialAverage_isShownWithoutAnAttemptThatCountsToIt() {
		// A qualitative approval counts toward no average, so the only average this term has is the one DST printed.
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(qualitative(id = "a1", code = "ID1101", credits = 2, symbol = "A"))
			).copy(officialPeriodAverage = 2.8)
		)

		assertEquals(2.8, stats.terms.single().index)
	}

	@Test
	fun cumulativeIndex_isNullUntilSomethingIsGraded_thenFollowsTheAverage() {
		val stats = statisticsOf(
			term(
				id = "t1",
				year = 2023,
				attempts = listOf(unreported(id = "a1", code = "MA1111", credits = 3))
			),
			term(
				id = "t2",
				year = 2024,
				attempts = listOf(numeric(id = "a2", code = "CI2525", credits = 3, value = 5, outcome = AttemptOutcome.APPROVED))
			)
		)

		assertNull(stats.terms.first().cumulativeIndex)
		assertEquals(5.0, stats.terms.last().cumulativeIndex)
	}

	@Test
	fun currentTerm_isLeftOutOfEverything() {
		val stats = statisticsOf(
			term(
				id = "closed",
				year = 2024,
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
			),
			term(
				id = "current",
				year = 2025,
				kind = TermKind.CURRENT,
				attempts = listOf(
					numeric(id = "a2", code = "MA1111", credits = 3, value = 5, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a3", code = "CI2525", credits = 2, value = 5, outcome = AttemptOutcome.APPROVED)
				)
			)
		)

		assertEquals(listOf(2024), stats.terms.map(TermStatistic::periodYear))
		assertEquals(1, stats.subjects.failed)
		assertEquals(0, stats.subjects.approved)
		assertEquals(1, stats.subjects.firstTryBase)
		assertEquals(1, stats.grades.countsByGrade.getValue(2))
		assertEquals(0, stats.grades.countsByGrade.getValue(5))
	}

	@Test
	fun syntheticTerm_isLeftOutOfEverything_evenWhenItIsInTheProjection() {
		val synthetic = TermProjection(
			id = "synthetic",
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC,
			termKey = "2026-SEP_DEC",
			termOrder = 20266,
			periodLabel = "Septiembre - Diciembre 2026",
			kind = TermKind.SYNTHETIC,
			periodAverage = 5.0,
			cumulativeAverage = 5.0,
			periodCredits = 3,
			cumulativeCredits = 3,
			attempts = listOf(
				AttemptProjection(
					id = "synthetic-attempt",
					subjectCode = "MA1111",
					subjectName = "MA1111",
					credits = 3,
					gradingMode = AttemptGradingMode.NUMERIC,
					score = AttemptScore.numeric(5),
					outcome = AttemptOutcome.APPROVED,
					badge = AttemptBadge.NONE
				)
			)
		)

		val stats = RecordStatisticsEngine.statisticsOf(RecordProjection(terms = listOf(synthetic)))

		assertEquals(emptyList<TermStatistic>(), stats.terms)
		assertEquals(0, stats.subjects.approved)
		assertEquals(0, stats.subjects.noResult)
		assertEquals(0, stats.grades.countsByGrade.getValue(5))
	}

	@Test
	fun officialAverages_areShownAsDstPrintedThem() {
		// Derived from the attempt alone: 3.0. DST printed 2.8 for the term and 3.5 accumulated.
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 4, value = 3, outcome = AttemptOutcome.APPROVED))
			).copy(officialPeriodAverage = 2.8, officialCumulativeAverage = 3.5)
		)

		val closed = stats.terms.single()
		assertEquals(2.8, closed.index)
		assertEquals(3.5, closed.cumulativeIndex)
	}

	@Test
	fun termsAreChronological_andTheOrderOfTheProjectionDoesNotMatter() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				term(
					id = "t2025",
					year = 2025,
					attempts = listOf(numeric(id = "a2", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED))
				),
				term(
					id = "t2024",
					year = 2024,
					attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
				)
			)
		)
		val projection = RecordProjectionEngine.projectAcademic(record)

		val stats = RecordStatisticsEngine.statisticsOf(projection)

		assertEquals(listOf(2024, 2025), stats.terms.map(TermStatistic::periodYear))
		assertEquals(0, stats.subjects.approvedFirstTry)
		assertEquals(stats, RecordStatisticsEngine.statisticsOf(RecordProjection(terms = projection.terms.reversed())))
	}

	@Test
	fun julyAugustTerm_isNotRegular_andTheOtherTermsAre() {
		val stats = statisticsOf(
			term(id = "summer", year = 2024, period = AcademicTermPeriod.JUL_AUG),
			term(id = "autumn", year = 2024, period = AcademicTermPeriod.SEP_DEC)
		)

		assertEquals(listOf(false, true), stats.terms.map(TermStatistic::isRegular))
	}

	@Test
	fun termApprovals_countEachSubjectOnce() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a2", code = "CI2525", credits = 2, value = 5, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a3", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED)
				)
			)
		)

		val closed = stats.terms.single()
		assertEquals(5, closed.approvedCredits)
		assertEquals(2, closed.approvedSubjects)
	}

	@Test
	fun creditsOfASubject_countOnce_whateverItsAttempts() {
		val stats = statisticsOf(
			term(
				id = "t1",
				year = 2023,
				attempts = listOf(numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
			),
			term(
				id = "t2",
				year = 2024,
				attempts = listOf(numeric(id = "a2", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED))
			)
		)

		assertEquals(1, stats.subjects.failed)
		assertEquals(3, stats.credits.failed)
		assertEquals(1, stats.subjects.repeated)
		assertEquals(2, stats.grades.countsByGrade.getValue(2))
	}

	@Test
	fun histogram_countsEveryNumericResult_andKeepsEveryGradeKey() {
		val stats = statisticsOf(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 5, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a2", code = "CI2525", credits = 2, value = 5, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a3", code = "FS1111", credits = 3, value = 3, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a4", code = "ID1101", credits = 2, value = 1, outcome = AttemptOutcome.FAILED),
					numeric(id = "a5", code = "CI1111", credits = 2, value = 0, outcome = AttemptOutcome.FAILED)
				)
			)
		)

		assertEquals(mapOf(1 to 1, 2 to 0, 3 to 1, 4 to 0, 5 to 2), stats.grades.countsByGrade)
	}

	@Test
	fun emptyProjection_hasNoStatistics_andEveryGradeKey() {
		val stats = RecordStatisticsEngine.statisticsOf(RecordProjection())

		assertEquals(emptyList<TermStatistic>(), stats.terms)
		assertEquals(
			SubjectOutcomes(
				approved = 0,
				failed = 0,
				withdrawn = 0,
				noResult = 0,
				approvedFirstTry = 0,
				firstTryBase = 0,
				repeated = 0
			),
			stats.subjects
		)
		assertEquals(CreditOutcomes(approved = 0, failed = 0, withdrawn = 0), stats.credits)
		assertEquals(
			GradeHistogram(countsByGrade = (1..MAX_SUBJECT_GRADE).associateWith { 0 }, unreported = 0),
			stats.grades
		)
	}
}

private fun statisticsOf(vararg terms: AcademicTerm): RecordStatistics {
	val record = AcademicRecord(id = "record", terms = terms.toList())

	return RecordStatisticsEngine.statisticsOf(RecordProjectionEngine.projectAcademic(record))
}

private fun term(
	id: String,
	year: Int = 2024,
	period: AcademicTermPeriod = AcademicTermPeriod.SEP_DEC,
	kind: TermKind = TermKind.HISTORICAL,
	attempts: List<AcademicAttempt> = emptyList()
): AcademicTerm {
	return AcademicTerm(
		id = id,
		periodYear = year,
		periodCode = period,
		kind = kind,
		attempts = attempts
	)
}

private fun numeric(
	id: String,
	code: String,
	credits: Int,
	value: Int,
	outcome: AttemptOutcome
): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = credits,
		gradingMode = AttemptGradingMode.NUMERIC,
		academicScore = AttemptScore.numeric(value),
		academicOutcome = outcome
	)
}

private fun qualitative(
	id: String,
	code: String,
	credits: Int,
	symbol: String
): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = credits,
		gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
		academicScore = AttemptScore.symbolic(symbol)
	)
}

private fun unreported(
	id: String,
	code: String,
	credits: Int
): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = credits,
		gradingMode = AttemptGradingMode.NUMERIC,
		academicScore = AttemptScore.empty(),
		academicOutcome = AttemptOutcome.UNREPORTED
	)
}
