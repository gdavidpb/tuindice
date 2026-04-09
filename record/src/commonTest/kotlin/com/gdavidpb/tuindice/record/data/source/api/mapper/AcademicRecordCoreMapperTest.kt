package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicProfile
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordCoreMapperTest {
	@Test
	fun toVersionedAcademicRecord_preservesRevisionAndCanonicalRecord() {
		val record = AcademicRecord(
			id = CURRENT_ACADEMIC_RECORD_ID,
				profile = AcademicProfile(
					userId = "u1",
					firstNames = "Ada",
					lastNames = "Lovelace",
					careerName = "Ingenieria",
					careerCode = 12039
				),
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					label = "Abril - Julio 2026",
					startAtMillis = 1_710_000_000_000L,
					endAtMillis = 1_720_000_000_000L,
					kind = TermKind.OFFICIAL_CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = "attempt-1",
							subjectCode = "MA1116",
							subjectName = "Calculo",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC,
							officialScore = AttemptScore.numeric(5),
							officialOutcome = AttemptOutcome.APPROVED,
							officialBadge = AttemptBadge.NONE
						)
					)
				)
			),
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = "attempt-1",
					score = AttemptScore.numeric(4),
					outcome = AttemptOutcome.PENDING,
					updatedAtMillis = 42L
				)
			)
		)

		val versioned = AcademicRecordResponse(
			revision = 12L,
			record = record
		).toVersionedAcademicRecord()

		assertEquals(12L, versioned.revision)
		assertEquals(record, versioned.record)
	}

	@Test
	fun buildAcademicUpsertAttemptOverrideRequest_preservesStructuredPayload() {
		val request = buildAcademicUpsertAttemptOverrideRequest(
			score = AttemptScore.symbolic("A"),
			outcome = AttemptOutcome.APPROVED
		)

		assertEquals(AttemptScore.symbolic("A"), request.score)
		assertEquals(AttemptOutcome.APPROVED, request.outcome)
	}
}
