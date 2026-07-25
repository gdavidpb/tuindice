package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicProfile
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.record.data.source.api.response.SyntheticTermLoadPreviewResponse
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBasis
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadConfidence
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadDetail
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordCoreMapperTest {
	private val json = Json { ignoreUnknownKeys = true }

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
					periodYear = 2024,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = "attempt-1",
							subjectCode = "MA1116",
							subjectName = "Calculo",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC,
							academicScore = AttemptScore.numeric(5),
							academicOutcome = AttemptOutcome.APPROVED,
							academicBadge = AttemptBadge.NONE
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
	fun academicRecordResponse_decodesLongAcademicTermPeriods() {
		val response = json.decodeFromString<AcademicRecordResponse>(
			"""
			{
			  "revision": 1,
			  "record": {
			    "id": "u1",
			    "terms": [
			      {
			        "id": "term-apr-sep",
			        "period_year": 2012,
			        "period_code": "APR_SEP",
			        "term_kind": "historical",
			        "attempts": [],
			        "term_key": "2012-APR_SEP",
			        "term_order": 20125,
			        "period_label": "Abril - Septiembre 2012"
			      },
			      {
			        "id": "term-jul-dec",
			        "period_year": 2026,
			        "period_code": "JUL_DEC",
			        "term_kind": "historical",
			        "attempts": [],
			        "term_key": "2026-JUL_DEC",
			        "term_order": 20267,
			        "period_label": "Julio - Diciembre 2026"
			      },
			      {
			        "id": "term-jan-may",
			        "period_year": 2025,
			        "period_code": "JAN_MAY",
			        "term_kind": "historical",
			        "attempts": [],
			        "term_key": "2025-JAN_MAY",
			        "term_order": 20252,
			        "period_label": "Enero - Mayo 2025"
			      }
			    ]
			  }
			}
			""".trimIndent()
		)

		assertEquals(
			listOf(AcademicTermPeriod.APR_SEP, AcademicTermPeriod.JUL_DEC, AcademicTermPeriod.JAN_MAY),
			response.record.terms.map(AcademicTerm::periodCode)
		)
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
	fun toSyntheticTermLoadPreview_preservesConfidenceMetadata() {
		val preview = SyntheticTermLoadPreviewResponse(
			available = true,
			band = "NORMAL",
			credits = 12,
			weightedDifficulty = 30.0,
			loadIndex = 15.6,
			baselineLoadIndex = 14.0,
			effectiveTerms = 4,
			basis = "PERSONAL",
			confidence = "HIGH",
			detail = "PERSONAL_HISTORY_STRONG"
		).toSyntheticTermLoadPreview()

		assertEquals(true, preview.available)
		assertEquals(SyntheticTermLoadBand.NORMAL, preview.band)
		assertEquals(SyntheticTermLoadBasis.PERSONAL, preview.basis)
		assertEquals(SyntheticTermLoadConfidence.HIGH, preview.confidence)
		assertEquals(SyntheticTermLoadDetail.PERSONAL_HISTORY_STRONG, preview.detail)
	}

	@Test
	fun addSyntheticTermRequest_includesMutationMetadata() {
		val request = AcademicRecordMutation.AddSyntheticTerm(
			termId = "term-1",
			periodYear = 2027,
			periodCode = AcademicTermPeriod.JUL_AUG,
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

		assertEquals("mutation-2", request.mutationId)
		assertEquals(8L, request.expectedRevision)
		assertEquals(2027, request.periodYear)
		assertEquals(AcademicTermPeriod.JUL_AUG, request.periodCode)
		assertEquals(listOf("MAT101"), request.subjectCodes)
	}

	@Test
	fun updateSyntheticTermRequest_includesMutationMetadata() {
		val request = AcademicRecordMutation.UpdateSyntheticTerm(
			targetTermId = "2027-JUL_AUG",
			targetTermKey = "2027-JUL_AUG",
			termId = "2027-SEP_DEC",
			periodYear = 2027,
			periodCode = AcademicTermPeriod.SEP_DEC,
			attempts = listOf(
				AcademicRecordMutation.UpdateSyntheticTerm.SyntheticAttemptSeed(
					attemptId = "attempt-1",
					subjectCode = "MAT101",
					subjectName = "Calculo I",
					credits = 4,
					gradingMode = AttemptGradingMode.NUMERIC,
					score = AttemptScore.empty(),
					outcome = AttemptOutcome.PENDING
				)
			)
		).toUpdateSyntheticTermRequest(
			mutationId = "mutation-4",
			expectedRevision = 10L
		)

		assertEquals("mutation-4", request.mutationId)
		assertEquals(10L, request.expectedRevision)
		assertEquals(2027, request.periodYear)
		assertEquals(AcademicTermPeriod.SEP_DEC, request.periodCode)
		assertEquals(listOf("MAT101"), request.subjectCodes)
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
