package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordProjectionEngineTest {
	@Test
	fun projectWorking_countsPendingQualitativeCreditsInPeriodCredits_withoutAffectingAverage() {
		val projection = projectSingleTerm(
			attempt(
				id = "qualitative-pending",
				credits = 9,
				gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL
			),
			attempt(
				id = "numeric-approved",
				credits = 4,
				score = AttemptScore.numeric(5),
				outcome = AttemptOutcome.APPROVED
			)
		)

		assertEquals(5.0, projection.periodAverage)
		assertEquals(13, projection.periodCredits)
		assertEquals(5.0, projection.cumulativeAverage)
		assertEquals(4, projection.cumulativeCredits)
	}

	@Test
	fun projectWorking_excludesRetiredQualitativeCreditsFromPeriodCredits() {
		val projection = projectSingleTerm(
			attempt(
				id = "qualitative-retired",
				credits = 9,
				gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
				outcome = AttemptOutcome.RETIRED
			),
			attempt(
				id = "numeric-approved",
				credits = 4,
				score = AttemptScore.numeric(4),
				outcome = AttemptOutcome.APPROVED
			)
		)

		assertEquals(4.0, projection.periodAverage)
		assertEquals(4, projection.periodCredits)
		assertEquals(4.0, projection.cumulativeAverage)
		assertEquals(4, projection.cumulativeCredits)
	}

	@Test
	fun projectWorking_keepsEmptySyntheticTermOnceEarlierAttemptIsApprovedByOverride() {
		val projection = RecordProjectionEngine.projectWorking(
			record(
				terms = listOf(
					term(
						id = "apr-jul-2026",
						startAtMillis = 1L,
						endAtMillis = 2L,
						kind = TermKind.OFFICIAL_CURRENT,
						attempts = listOf(
							attempt(
								id = "ep5406-official",
								subjectCode = "EP5406",
								credits = 9,
								gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
								outcome = AttemptOutcome.FAILED
							)
						)
					),
					term(
						id = "sep-dic-2026",
						startAtMillis = 3L,
						endAtMillis = 4L,
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							attempt(
								id = "ep5406-synthetic",
								subjectCode = "EP5406",
								credits = 9,
								gradingMode = AttemptGradingMode.QUALITATIVE_PASS_FAIL,
								outcome = AttemptOutcome.APPROVED
							)
						)
					)
				),
				attemptOverrides = listOf(
					AttemptOverride(
						attemptId = "ep5406-official",
						outcome = AttemptOutcome.APPROVED,
						updatedAtMillis = 10L
					)
				)
			)
		)

		assertEquals(listOf("sep-dic-2026", "apr-jul-2026"), projection.terms.map { term -> term.id })
		assertEquals(emptyList(), projection.terms.first().attempts)
		assertEquals(AttemptOutcome.APPROVED, projection.terms.last().attempts.single().outcome)
		assertEquals(AttemptBadge.NONE, projection.terms.last().attempts.single().badge)
	}

	@Test
	fun projectWorking_omitsOnlyObsoleteSyntheticAttemptsWhenTermStillHasOtherSubjects() {
		val projection = RecordProjectionEngine.projectWorking(
			record(
				terms = listOf(
					term(
						id = "synthetic-1",
						startAtMillis = 1L,
						endAtMillis = 2L,
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							attempt(
								id = "mat-approved",
								subjectCode = "MAT2205",
								credits = 5,
								score = AttemptScore.numeric(5),
								outcome = AttemptOutcome.APPROVED
							)
						)
					),
					term(
						id = "synthetic-2",
						startAtMillis = 3L,
						endAtMillis = 4L,
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							attempt(
								id = "mat-obsolete",
								subjectCode = "MAT2205",
								credits = 5,
								score = AttemptScore.numeric(2),
								outcome = AttemptOutcome.FAILED
							),
							attempt(
								id = "fis-pending",
								subjectCode = "FIS1001",
								credits = 4
							)
						)
					)
				)
			)
		)

		assertEquals(listOf("synthetic-2", "synthetic-1"), projection.terms.map { term -> term.id })
		assertEquals(listOf("fis-pending"), projection.terms.first().attempts.map { attempt -> attempt.id })
	}
}

private fun projectSingleTerm(vararg attempts: AcademicAttempt) = RecordProjectionEngine.projectWorking(
	record(
		terms = listOf(
			term(
				id = "term-1",
				startAtMillis = 1L,
				endAtMillis = 2L,
				kind = TermKind.OFFICIAL_CURRENT,
				attempts = attempts.toList()
			)
		)
	)
).terms.single()

private fun record(
	terms: List<AcademicTerm>,
	attemptOverrides: List<AttemptOverride> = emptyList()
) = AcademicRecord(
	id = "record-1",
	terms = terms,
	attemptOverrides = attemptOverrides
)

private fun term(
	id: String,
	startAtMillis: Long,
	endAtMillis: Long,
	kind: TermKind,
	attempts: List<AcademicAttempt>
) = AcademicTerm(
	id = id,
	periodYear = 2020 + startAtMillis.toInt(),
	periodCode = AcademicTermPeriod.JAN_MAR,
	kind = kind,
	termKey = id,
	termOrder = startAtMillis.toInt(),
	periodLabel = id,
	attempts = attempts
)

private fun attempt(
	id: String,
	subjectCode: String = id.uppercase(),
	credits: Int,
	gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
	score: AttemptScore = AttemptScore.empty(),
	outcome: AttemptOutcome = AttemptOutcome.PENDING
) = AcademicAttempt(
	id = id,
	subjectCode = subjectCode,
	subjectName = id,
	credits = credits,
	gradingMode = gradingMode,
	officialScore = score,
	officialOutcome = outcome
)
