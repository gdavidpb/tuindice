package com.gdavidpb.tuindice.academiccore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicPensumSnapshotTest {
	@Test
	fun toAcademicPensumSnapshot_leavesWithdrawnAttemptsOut_whateverTheirResult() {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				AcademicTerm(
					id = "historical",
					periodYear = 2025,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.HISTORICAL,
					attempts = listOf(
						attempt(subjectCode = "CC1001", outcome = AttemptOutcome.APPROVED, withdrawn = true),
						attempt(subjectCode = "DD1001", outcome = AttemptOutcome.APPROVED, withdrawn = false)
					)
				),
				AcademicTerm(
					id = "current",
					periodYear = 2025,
					periodCode = AcademicTermPeriod.APR_JUL,
					kind = TermKind.CURRENT,
					attempts = listOf(
						attempt(subjectCode = "BB1001", outcome = AttemptOutcome.PENDING, withdrawn = true),
						attempt(subjectCode = "AA1001", outcome = AttemptOutcome.PENDING, withdrawn = false)
					)
				)
			)
		)

		val snapshot = record.toAcademicPensumSnapshot()

		assertEquals(
			listOf("DD1001" to 1, "AA1001" to 1),
			snapshot.attempts.map { attempt -> attempt.subjectCode to attempt.positionInTerm }
		)
	}

	private fun attempt(subjectCode: String, outcome: AttemptOutcome, withdrawn: Boolean): AcademicAttempt {
		return AcademicAttempt(
			id = "attempt-$subjectCode",
			subjectCode = subjectCode,
			subjectName = subjectCode,
			credits = 4,
			academicOutcome = outcome,
			withdrawn = withdrawn
		)
	}
}
