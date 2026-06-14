package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Hand-computed cases pinning the engine's exact semantics; every expected value is
 * derived in the comment next to it.
 */
class RecordProjectionEngineGoldenTest {
	@Test
	fun emptyRecord_projectsToEmptyProjection() {
		val record = AcademicRecord(id = "record")

		assertEquals(RecordProjection(), RecordProjectionEngine.projectAcademic(record))
		assertEquals(RecordProjection(), RecordProjectionEngine.projectProjection(record))
	}

	@Test
	fun singleTerm_twoApprovedNumericAttempts_averagesWeightedByCredits() {
		// (4*3 + 5*2) / (3 + 2) = 22 / 5 = 4.4
		val record = record(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a2", code = "CI2525", credits = 2, value = 5, outcome = AttemptOutcome.APPROVED)
				)
			)
		)

		val term = RecordProjectionEngine.projectAcademic(record).terms.single()

		assertEquals(4.4, term.periodAverage)
		assertEquals(4.4, term.cumulativeAverage)
		assertEquals(5, term.periodCredits)
		assertEquals(5, term.cumulativeCredits)
	}

	@Test
	fun averages_truncateAtFourDecimals_insteadOfRounding() {
		// (3*1 + 4*2) / 3 = 11/3 = 3.66666... -> truncated to 3.6666 (rounding would give 3.6667)
		val record = record(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 1, value = 3, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a2", code = "CI2525", credits = 2, value = 4, outcome = AttemptOutcome.APPROVED)
				)
			)
		)

		assertEquals(3.6666, RecordProjectionEngine.projectAcademic(record).terms.single().periodAverage)
	}

	@Test
	fun unreportedAttempt_dragsPeriodAverage_butStaysOutOfCumulative() {
		// Period: (4*3 + 0*3) / (3 + 3) = 2.0 — UNREPORTED counts with zero contribution.
		// Cumulative: 12 / 3 = 4.0 — UNREPORTED is excluded from the cumulative average.
		val record = record(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED),
					AcademicAttempt(
						id = "a2",
						subjectCode = "CI2525",
						subjectName = "CI2525",
						credits = 3,
						gradingMode = AttemptGradingMode.NUMERIC,
						academicScore = AttemptScore.empty(),
						academicOutcome = AttemptOutcome.UNREPORTED
					)
				)
			)
		)

		val term = RecordProjectionEngine.projectAcademic(record).terms.single()

		assertEquals(2.0, term.periodAverage)
		assertEquals(4.0, term.cumulativeAverage)
		assertEquals(6, term.periodCredits)
		assertEquals(3, term.cumulativeCredits)
	}

	@Test
	fun retiredAttempt_countsNowhere_notEvenDisplayedCredits() {
		val record = record(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED),
					numeric(id = "a2", code = "CI2525", credits = 3, value = 2, outcome = AttemptOutcome.RETIRED)
				)
			)
		)

		val term = RecordProjectionEngine.projectAcademic(record).terms.single()

		assertEquals(4.0, term.periodAverage)
		assertEquals(4.0, term.cumulativeAverage)
		assertEquals(3, term.periodCredits)
		assertEquals(3, term.cumulativeCredits)
	}

	@Test
	fun retakeWithoutApproval_keepsBothAttempts_inCumulative() {
		// (2*3 + 1*3) / 6 = 9/6 = 1.5 — failing twice accumulates both attempts.
		val record = record(
			term(
				id = "t1",
				year = 2020,
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED)
				)
			),
			term(
				id = "t2",
				year = 2021,
				attempts = listOf(
					numeric(id = "a2", code = "MA1111", credits = 3, value = 1, outcome = AttemptOutcome.FAILED)
				)
			)
		)

		val latest = RecordProjectionEngine.projectAcademic(record).terms.first()

		assertEquals(1.5, latest.cumulativeAverage)
		assertEquals(6, latest.cumulativeCredits)
	}

	@Test
	fun approvingARetake_annulsThePreviousAttempt_andBadgesItOnlyInProjectionMode() {
		// Approval supersedes the failed attempt: cumulative = 4*3 / 3 = 4.0, credits 3.
		val record = record(
			term(
				id = "t1",
				year = 2020,
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 2, outcome = AttemptOutcome.FAILED)
				)
			),
			term(
				id = "t2",
				year = 2021,
				attempts = listOf(
					numeric(id = "a2", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED)
				)
			)
		)

		val academic = RecordProjectionEngine.projectAcademic(record)
		val academicLatest = academic.terms.first()
		assertEquals(4.0, academicLatest.cumulativeAverage)
		assertEquals(3, academicLatest.cumulativeCredits)

		val academicFailedAttempt = academic.terms.last().attempts.single()
		assertEquals(AttemptBadge.NONE, academicFailedAttempt.badge)

		val projection = RecordProjectionEngine.projectProjection(record)
		val projectedFailedAttempt = projection.terms.last().attempts.single()
		assertEquals(AttemptBadge.WITHOUT_EFFECT, projectedFailedAttempt.badge)
	}

	@Test
	fun syntheticAttempts_forAlreadyApprovedSubjects_areFilteredOut() {
		val record = record(
			term(
				id = "t1",
				year = 2020,
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED)
				)
			),
			term(
				id = "t2",
				year = 2021,
				kind = TermKind.SYNTHETIC,
				attempts = listOf(
					numeric(id = "a2", code = "MA1111", credits = 3, value = 5, outcome = AttemptOutcome.PENDING)
				)
			)
		)

		val syntheticTerm = RecordProjectionEngine.projectProjection(record)
			.terms.first { term -> term.id == "t2" }

		assertTrue(
			syntheticTerm.attempts.isEmpty(),
			"Synthetic attempt for an approved subject must be filtered, got ${syntheticTerm.attempts}"
		)
	}

	@Test
	fun qualitativeApprovedAttempt_countsDisplayedCredits_butNotAverages() {
		val record = record(
			term(
				id = "t1",
				attempts = listOf(
					numeric(id = "a1", code = "MA1111", credits = 3, value = 4, outcome = AttemptOutcome.APPROVED),
					AcademicAttempt(
						id = "a2",
						subjectCode = "ID1101",
						subjectName = "ID1101",
						credits = 2,
						gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
						academicScore = AttemptScore.symbolic("A"),
						academicOutcome = AttemptOutcome.PENDING
					)
				)
			)
		)

		val term = RecordProjectionEngine.projectAcademic(record).terms.single()

		assertEquals(4.0, term.periodAverage)
		assertEquals(4.0, term.cumulativeAverage)
		assertEquals(5, term.periodCredits)
		assertEquals(3, term.cumulativeCredits)

		val qualitative = term.attempts.first { attempt -> attempt.id == "a2" }
		assertEquals(AttemptOutcome.APPROVED, qualitative.outcome)
	}

	@Test
	fun pendingEmptyNumericAttempt_inCurrentTerm_defaultsToFive_onlyInProjectionMode() {
		val record = record(
			term(
				id = "t1",
				kind = TermKind.CURRENT,
				attempts = listOf(
					AcademicAttempt(
						id = "a1",
						subjectCode = "MA1111",
						subjectName = "MA1111",
						credits = 4,
						gradingMode = AttemptGradingMode.NUMERIC,
						academicScore = AttemptScore.empty(),
						academicOutcome = AttemptOutcome.PENDING
					)
				)
			)
		)

		val academicTerm = RecordProjectionEngine.projectAcademic(record).terms.single()
		assertEquals(0.0, academicTerm.periodAverage)
		assertEquals(0, academicTerm.periodCredits)
		assertEquals(AttemptOutcome.PENDING, academicTerm.attempts.single().outcome)

		val projectedTerm = RecordProjectionEngine.projectProjection(record).terms.single()
		assertEquals(5.0, projectedTerm.periodAverage)
		assertEquals(4, projectedTerm.periodCredits)
		assertEquals(AttemptScore.numeric(5), projectedTerm.attempts.single().score)
		assertEquals(AttemptOutcome.APPROVED, projectedTerm.attempts.single().outcome)
	}
}

private fun record(vararg terms: AcademicTerm): AcademicRecord {
	return AcademicRecord(id = "record", terms = terms.toList())
}

private fun term(
	id: String,
	year: Int = 2024,
	kind: TermKind = TermKind.HISTORICAL,
	attempts: List<AcademicAttempt>
): AcademicTerm {
	return AcademicTerm(
		id = id,
		periodYear = year,
		periodCode = AcademicTermPeriod.SEP_DEC,
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
