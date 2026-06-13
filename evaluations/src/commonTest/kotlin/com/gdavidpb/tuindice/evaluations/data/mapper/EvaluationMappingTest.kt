package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.model.AddEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.DeleteEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.EvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.model.GetEvaluationsResponse
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.UpdateEvaluationResponse
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_REMOTE_PENDING_EVALUATION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationMappingTest {
	@Test
	fun toRemoteEvaluation_whenResponseIsMapped_copiesEveryField() {
		val remote = evaluationResponse().toRemoteEvaluation()

		assertEquals(
			RemoteEvaluation(
				id = "evaluation-1",
				referenceId = "reference-1",
				attemptId = "subject-1",
				subjectCode = "INF-101",
				termId = "quarter-1",
				revision = 7L,
				scheduleMode = EvaluationScheduleMode.DATED,
				grade = 18.0,
				maxGrade = 20.0,
				date = EVALUATION_DATE,
				type = EvaluationType.QUIZ.ordinal,
				isDone = true
			),
			remote
		)
	}

	@Test
	fun toRemoteEvaluationsSnapshot_whenResponseHasEvaluations_mapsThemAll() {
		val snapshot = GetEvaluationsResponse(
			evaluations = listOf(evaluationResponse())
		).toRemoteEvaluationsSnapshot()

		assertEquals(listOf(evaluationResponse().toRemoteEvaluation()), snapshot.evaluations)
	}

	@Test
	fun toRemoteEvaluationsSnapshot_whenResponseIsEmpty_mapsToEmptySnapshot() {
		val snapshot = GetEvaluationsResponse(evaluations = emptyList()).toRemoteEvaluationsSnapshot()

		assertTrue(snapshot.evaluations.isEmpty())
	}

	@Test
	fun toAddEvaluationRequest_whenGradeIsKnown_marksRequestAsDone() {
		val request = addMutation(grade = 15.0).toAddEvaluationRequest(mutationId = "mutation-1")

		assertEquals("reference-1", request.referenceId)
		assertEquals("subject-1", request.attemptId)
		assertEquals("quarter-1", request.termId)
		assertEquals(EvaluationScheduleMode.DATED, request.scheduleMode)
		assertEquals(15.0, request.grade)
		assertEquals(20.0, request.maxGrade)
		assertEquals(EVALUATION_DATE, request.date)
		assertEquals(EvaluationType.QUIZ.ordinal, request.type)
		assertEquals("mutation-1", request.mutationId)
		assertTrue(request.isDone)
	}

	@Test
	fun toAddEvaluationRequest_whenGradeIsMissing_marksRequestAsPending() {
		val request = addMutation(grade = null).toAddEvaluationRequest(mutationId = "mutation-1")

		assertNull(request.grade)
		assertFalse(request.isDone)
	}

	@Test
	fun toUpdateEvaluationRequest_whenFieldsAreSet_mapsThemWithExpectedRevision() {
		val request = EvaluationMutation.Update(
			evaluationId = "evaluation-1",
			scheduleMode = EvaluationScheduleMode.DATED,
			grade = 12.5,
			maxGrade = 25.0,
			date = EVALUATION_DATE,
			type = EvaluationType.TEST.ordinal
		).toUpdateEvaluationRequest(mutationId = "mutation-2", expectedRevision = 9L)

		assertEquals(EvaluationScheduleMode.DATED, request.scheduleMode)
		assertEquals(12.5, request.grade)
		assertEquals(25.0, request.maxGrade)
		assertEquals(EVALUATION_DATE, request.date)
		assertEquals(EvaluationType.TEST.ordinal, request.type)
		assertEquals(true, request.isDone)
		assertEquals("mutation-2", request.mutationId)
		assertEquals(9L, request.expectedRevision)
	}

	@Test
	fun toUpdateEvaluationRequest_whenOptionalFieldsAreNull_keepsThemNullAndPending() {
		val request = EvaluationMutation.Update(
			evaluationId = "evaluation-1",
			scheduleMode = null,
			grade = null,
			maxGrade = null,
			date = null,
			type = null
		).toUpdateEvaluationRequest(mutationId = "mutation-2", expectedRevision = 3L)

		assertNull(request.scheduleMode)
		assertNull(request.grade)
		assertNull(request.maxGrade)
		assertNull(request.date)
		assertNull(request.type)
		assertEquals(false, request.isDone)
	}

	@Test
	fun toMutationAck_whenResponsesAreMapped_carryMutationIdAndPayload() {
		val addAck = AddEvaluationResponse(
			mutationId = "mutation-1",
			evaluationPatch = evaluationResponse()
		).toMutationAck()
		val updateAck = UpdateEvaluationResponse(
			mutationId = "mutation-2",
			evaluationPatch = evaluationResponse()
		).toMutationAck()
		val removeAck = DeleteEvaluationResponse(
			mutationId = "mutation-3",
			removedEvaluationId = "evaluation-1"
		).toMutationAck()

		assertEquals("mutation-1", addAck.mutationId)
		assertEquals(evaluationResponse().toRemoteEvaluation(), addAck.evaluation)
		assertEquals("mutation-2", updateAck.mutationId)
		assertEquals(evaluationResponse().toRemoteEvaluation(), updateAck.evaluation)
		assertEquals("mutation-3", removeAck.mutationId)
		assertEquals("evaluation-1", removeAck.removedEvaluationId)
	}

	@Test
	fun toLocalEvaluation_whenRemoteIsMapped_copiesEveryField() {
		assertEquals(
			DEFAULT_LOCAL_PENDING_EVALUATION,
			DEFAULT_REMOTE_PENDING_EVALUATION.toLocalEvaluation()
		)
	}

	@Test
	fun toEvaluation_whenTypeOrdinalIsLastEntry_mapsToOtherType() {
		val evaluation = DEFAULT_LOCAL_PENDING_EVALUATION.copy(
			type = EvaluationType.OTHER.ordinal
		).toEvaluation()

		assertEquals(EvaluationType.OTHER, evaluation.type)
	}

	@Test
	fun toEvaluation_whenDatedGradeAndDateVary_computesEvaluationState() {
		val pastDate = currentTimeMillis() - ONE_DAY_MILLIS
		val futureDate = currentTimeMillis() + ONE_DAY_MILLIS

		val completed = DEFAULT_LOCAL_PENDING_EVALUATION.copy(
			grade = 18.0,
			date = pastDate
		).toEvaluation()
		val overdue = DEFAULT_LOCAL_PENDING_EVALUATION.copy(
			grade = null,
			date = pastDate
		).toEvaluation()
		val pending = DEFAULT_LOCAL_PENDING_EVALUATION.copy(
			grade = null,
			date = futureDate
		).toEvaluation()

		assertEquals(EvaluationState.COMPLETED, completed.state)
		assertEquals(EvaluationState.OVERDUE, overdue.state)
		assertEquals(EvaluationState.PENDING, pending.state)
	}

	@Test
	fun toEvaluation_whenScheduleModeIsContinuous_mapsToContinuousState() {
		val evaluation = DEFAULT_LOCAL_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = null
		).toEvaluation()

		assertEquals(EvaluationState.CONTINUOUS, evaluation.state)
	}

	private fun evaluationResponse() = EvaluationResponse(
		id = "evaluation-1",
		referenceId = "reference-1",
		termId = "quarter-1",
		attemptId = "subject-1",
		subjectCode = "INF-101",
		revision = 7L,
		type = EvaluationType.QUIZ.ordinal,
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = 18.0,
		maxGrade = 20.0,
		date = EVALUATION_DATE,
		isDone = true
	)

	private fun addMutation(grade: Double?) = EvaluationMutation.Add(
		referenceId = "reference-1",
		attemptId = "subject-1",
		subjectCode = "INF-101",
		termId = "quarter-1",
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = grade,
		maxGrade = 20.0,
		date = EVALUATION_DATE,
		type = EvaluationType.QUIZ.ordinal
	)
}

private const val EVALUATION_DATE = 1_800_000_000_000L
private const val ONE_DAY_MILLIS = 86_400_000L
