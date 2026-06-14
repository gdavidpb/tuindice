package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class EvaluationParamsMappingTest {
	@Test
	fun toGetEvaluationParams_whenActionCarriesEvaluationId_mapsItThrough() {
		val action = Evaluation.Action.LoadEvaluation(evaluationId = "evaluation-1")

		assertEquals("evaluation-1", action.toGetEvaluationParams().evaluationId)
	}

	@Test
	fun toAddEvaluationParams_whenAttemptIsSelected_mapsAttemptAndFields() {
		val params = contentState(
			selectedAttempt = true,
			evaluationId = null
		).toAddEvaluationParams()

		assertEquals(DEFAULT_EVALUATION_SUBJECT.id, params.attemptId)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.code, params.subjectCode)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.termId, params.termId)
		assertEquals(EvaluationType.QUIZ, params.type)
		assertEquals(EvaluationScheduleMode.DATED, params.scheduleMode)
		assertEquals(EVALUATION_DATE, params.date)
		assertEquals(15.0, params.grade)
		assertEquals(20.0, params.maxGrade)
	}

	@Test
	fun toAddEvaluationParams_whenNoAttemptIsSelected_leavesAttemptFieldsNull() {
		val params = contentState(
			selectedAttempt = false,
			evaluationId = null
		).toAddEvaluationParams()

		assertNull(params.attemptId)
		assertNull(params.subjectCode)
		assertNull(params.termId)
	}

	@Test
	fun toUpdateEvaluationParams_whenEvaluationIdIsKnown_mapsAllFields() {
		val params = contentState(
			selectedAttempt = true,
			evaluationId = "evaluation-1"
		).toUpdateEvaluationParams()

		assertEquals("evaluation-1", params.evaluationId)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.id, params.attemptId)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.code, params.subjectCode)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.termId, params.termId)
		assertEquals(EvaluationScheduleMode.DATED, params.scheduleMode)
		assertEquals(15.0, params.grade)
		assertEquals(20.0, params.maxGrade)
		assertEquals(EVALUATION_DATE, params.date)
		assertEquals(EvaluationType.QUIZ, params.type)
	}

	@Test
	fun toUpdateEvaluationParams_whenEvaluationIdIsMissing_fails() {
		assertFailsWith<IllegalArgumentException> {
			contentState(
				selectedAttempt = true,
				evaluationId = null
			).toUpdateEvaluationParams()
		}
	}

	@Test
	fun toUpdateEvaluationParams_whenMappingGradeAction_onlyCarriesIdAndGrade() {
		val action = Evaluations.Action.SetEvaluationGrade(
			evaluationId = "evaluation-1",
			grade = 17.5
		)

		val params = action.toUpdateEvaluationParams()

		assertEquals("evaluation-1", params.evaluationId)
		assertEquals(17.5, params.grade)
		assertNull(params.attemptId)
		assertNull(params.subjectCode)
		assertNull(params.termId)
		assertNull(params.scheduleMode)
		assertNull(params.maxGrade)
		assertNull(params.date)
		assertNull(params.type)
	}

	private fun contentState(
		selectedAttempt: Boolean,
		evaluationId: String?
	) = Evaluation.State.Content(
		evaluationId = evaluationId,
		selectedAttempt = if (selectedAttempt) DEFAULT_EVALUATION_SUBJECT else null,
		type = EvaluationType.QUIZ,
		scheduleMode = EvaluationScheduleMode.DATED,
		date = EVALUATION_DATE,
		grade = 15.0,
		maxGrade = 20.0
	)
}

private const val EVALUATION_DATE = 1_800_000_000_000L
