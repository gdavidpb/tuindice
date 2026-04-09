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
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordCoreMapperTest {
	@Test
	fun toVersionedAcademicRecord_preservesRevisionAndRecordPayload() {
		val record = AcademicRecord(
			id = "u1",
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
			outcome = AttemptOutcome.APPROVED,
			mutationId = "mutation-1",
			expectedRevision = 7L
		)

		assertEquals(AttemptScore.symbolic("A"), request.score)
		assertEquals(AttemptOutcome.APPROVED, request.outcome)
		assertEquals("mutation-1", request.mutationId)
		assertEquals(7L, request.expectedRevision)
	}

	@Test
	fun addSyntheticTermRequest_includesMutationMetadata() {
		val request = AcademicRecordMutation.AddSyntheticTerm(
			termId = "term-1",
			label = "2026-Especial",
			startAtMillis = 1L,
			endAtMillis = 2L,
			attempts = listOf(
				AcademicRecordMutation.AddSyntheticTerm.SyntheticAttemptSeed(
					attemptId = "attempt-1",
					subjectCode = "MAT101",
					subjectName = "Calculo I",
					credits = 4,
					gradingMode = AttemptGradingMode.NUMERIC,
					score = AttemptScore.numeric(5),
					outcome = AttemptOutcome.APPROVED
				)
			)
		).toAddSyntheticTermRequest(
			mutationId = "mutation-2",
			expectedRevision = 8L
		)

		assertEquals("2026-Especial", request.label)
		assertEquals("mutation-2", request.mutationId)
		assertEquals(8L, request.expectedRevision)
		assertEquals("MAT101", request.attempts.first().subjectCode)
	}

	@Test
	fun buildDeleteOverlayMutationRequest_preservesMutationMetadata() {
		val request = buildDeleteOverlayMutationRequest(
			mutationId = "mutation-3",
			expectedRevision = 9L
		)

		assertEquals("mutation-3", request.mutationId)
		assertEquals(9L, request.expectedRevision)
	}
}
