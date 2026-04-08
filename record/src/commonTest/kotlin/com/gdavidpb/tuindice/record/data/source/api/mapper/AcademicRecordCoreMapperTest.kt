package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.HistoricalBadge
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AttemptGradingModeResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AttemptProjectionResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AttemptScoreKindResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AttemptScoreResponse
import com.gdavidpb.tuindice.record.data.source.api.response.HistoricalBadgeResponse
import com.gdavidpb.tuindice.record.data.source.api.response.OfficialOutcomeResponse
import com.gdavidpb.tuindice.record.data.source.api.response.RecordProjectionResponse
import com.gdavidpb.tuindice.record.data.source.api.response.TermProjectionResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcademicRecordCoreMapperTest {
	@Test
	fun `toAcademicRecord derives overrides and synthetic terms from projections`() {
		val officialAttempt = AttemptProjectionResponse(
			id = "attempt-1",
			termId = "term-official",
			subjectCode = "MA1116",
			subjectName = "Calculo",
			credits = 4,
			sequenceInTerm = 0,
			gradingMode = AttemptGradingModeResponse.NUMERIC,
			score = AttemptScoreResponse(
				kind = AttemptScoreKindResponse.NUMERIC,
				numericValue = 3
			),
			outcome = OfficialOutcomeResponse.APPROVED,
			badge = HistoricalBadgeResponse.NONE,
			editable = false
		)

		val simulationAttempt = officialAttempt.copy(
			score = AttemptScoreResponse(
				kind = AttemptScoreKindResponse.NUMERIC,
				numericValue = 5
			),
			outcome = OfficialOutcomeResponse.APPROVED,
			editable = true
		)

		val syntheticAttempt = AttemptProjectionResponse(
			id = "attempt-synthetic",
			termId = "term-synthetic",
			subjectCode = "ID9999",
			subjectName = "Electiva",
			credits = 3,
			sequenceInTerm = 0,
			gradingMode = AttemptGradingModeResponse.NUMERIC,
			score = AttemptScoreResponse(
				kind = AttemptScoreKindResponse.NUMERIC,
				numericValue = 4
			),
			outcome = OfficialOutcomeResponse.APPROVED,
			badge = HistoricalBadgeResponse.NONE,
			editable = true
		)

		val record = AcademicRecordResponse(
			revision = 12L,
			officialProjection = RecordProjectionResponse(
				terms = listOf(
					TermProjectionResponse(
						id = "term-official",
						label = "Abril - Julio 2026",
						startAt = 1_710_000_000_000L,
						endAt = 1_720_000_000_000L,
						kind = TermKind.OFFICIAL_HISTORICAL,
						grade = 3.0,
						gradeSum = 12.0,
						credits = 4,
						creditsSum = 4,
						attempts = listOf(officialAttempt)
					)
				)
			),
			simulationProjection = RecordProjectionResponse(
				terms = listOf(
					TermProjectionResponse(
						id = "term-synthetic",
						label = "Septiembre - Diciembre 2026",
						startAt = 1_730_000_000_000L,
						endAt = 1_740_000_000_000L,
						kind = TermKind.SYNTHETIC,
						grade = 4.0,
						gradeSum = 12.0,
						credits = 3,
						creditsSum = 3,
						attempts = listOf(syntheticAttempt)
					),
					TermProjectionResponse(
						id = "term-official",
						label = "Abril - Julio 2026",
						startAt = 1_710_000_000_000L,
						endAt = 1_720_000_000_000L,
						kind = TermKind.OFFICIAL_HISTORICAL,
						grade = 5.0,
						gradeSum = 20.0,
						credits = 4,
						creditsSum = 4,
						attempts = listOf(simulationAttempt)
					)
				)
			)
		).toAcademicRecord()

		assertEquals(1, record.officialSnapshot.terms.size)
		assertEquals(1, record.localOverlay.attemptOverrides.size)
		assertEquals("attempt-1", record.localOverlay.attemptOverrides.single().attemptId)
		assertEquals(AttemptScore.numeric(5), record.localOverlay.attemptOverrides.single().score)
		assertEquals(1, record.localOverlay.syntheticTerms.size)
		assertEquals("term-synthetic", record.localOverlay.syntheticTerms.single().id)
		assertTrue(record.simulationProjection.terms.any { term -> term.id == "term-synthetic" })
	}

	@Test
	fun `toAcademicRecord preserves official current term kind from the response`() {
		val record = AcademicRecordResponse(
			revision = 1L,
			officialProjection = RecordProjectionResponse(
				terms = listOf(
					TermProjectionResponse(
						id = "term-current",
						label = "Enero - Marzo 2026",
						startAt = 1_767_236_400_000L,
						endAt = 1_774_926_000_000L,
						kind = TermKind.OFFICIAL_CURRENT,
						grade = 0.0,
						gradeSum = 3.4,
						credits = 0,
						creditsSum = 120,
						attempts = emptyList()
					)
				)
			),
			simulationProjection = RecordProjectionResponse(
				terms = listOf(
					TermProjectionResponse(
						id = "term-current",
						label = "Enero - Marzo 2026",
						startAt = 1_767_236_400_000L,
						endAt = 1_774_926_000_000L,
						kind = TermKind.OFFICIAL_CURRENT,
						grade = 5.0,
						gradeSum = 3.5,
						credits = 6,
						creditsSum = 126,
						attempts = emptyList()
					)
				)
			)
		).toAcademicRecord()

		assertEquals(TermKind.OFFICIAL_CURRENT, record.officialSnapshot.terms.single().kind)
		assertEquals(TermKind.OFFICIAL_CURRENT, record.officialProjection.terms.single().kind)
		assertEquals(TermKind.OFFICIAL_CURRENT, record.simulationProjection.terms.single().kind)
	}

	@Test
	fun `response status mapper preserves without effect badge`() {
		val status = AttemptProjectionResponse(
			id = "attempt-1",
			termId = "term-1",
			subjectCode = "MA1116",
			subjectName = "Calculo",
			credits = 4,
			sequenceInTerm = 0,
			gradingMode = AttemptGradingModeResponse.NUMERIC,
			score = AttemptScoreResponse(
				kind = AttemptScoreKindResponse.NUMERIC,
				numericValue = 2
			),
			outcome = OfficialOutcomeResponse.FAILED,
			badge = HistoricalBadgeResponse.WITHOUT_EFFECT,
			editable = false
		).toUiStatus()

		assertEquals(com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus.WITHOUT_EFFECT, status)
	}
}
