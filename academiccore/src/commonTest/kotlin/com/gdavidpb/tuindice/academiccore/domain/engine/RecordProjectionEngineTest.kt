package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
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
}

private fun projectSingleTerm(vararg attempts: AcademicAttempt) = RecordProjectionEngine.projectWorking(
	AcademicRecord(
		id = "record-1",
		terms = listOf(
			AcademicTerm(
				id = "term-1",
				label = "2026-3",
				startAtMillis = 1L,
				endAtMillis = 2L,
				kind = TermKind.OFFICIAL_CURRENT,
				attempts = attempts.toList()
			)
		)
	)
).terms.single()

private fun attempt(
	id: String,
	credits: Int,
	gradingMode: AttemptGradingMode = AttemptGradingMode.NUMERIC,
	score: AttemptScore = AttemptScore.empty(),
	outcome: AttemptOutcome = AttemptOutcome.PENDING
) = AcademicAttempt(
	id = id,
	subjectCode = id.uppercase(),
	subjectName = id,
	credits = credits,
	gradingMode = gradingMode,
	officialScore = score,
	officialOutcome = outcome
)
