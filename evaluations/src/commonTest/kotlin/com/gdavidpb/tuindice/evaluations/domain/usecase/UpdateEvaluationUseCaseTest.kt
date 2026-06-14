package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateEvaluationUseCaseTest {
	@Test
	fun updateEvaluationUseCase_whenParamsAreValid_persistsMappedUpdate() = runTest {
		val evaluationRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION)
		)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)
		val pastDate = currentTimeMillis() - ONE_DAY_MILLIS
		val params = validParams(
			evaluationId = DEFAULT_PENDING_EVALUATION.id,
			grade = 30.0,
			date = pastDate
		)

		useCase.execute(params).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(
			EvaluationUpdate(
				id = DEFAULT_PENDING_EVALUATION.id,
				scheduleMode = EvaluationScheduleMode.DATED,
				grade = 30.0,
				maxGrade = 35.0,
				date = pastDate,
				type = EvaluationType.TEST
			),
			evaluationRepository.updateCalls.single()
		)

		val updated = assertNotNull(evaluationRepository.getEvaluation(DEFAULT_PENDING_EVALUATION.id))
		assertEquals(30.0, updated.grade)
		assertEquals(EvaluationType.TEST, updated.type)
		assertEquals(EvaluationState.COMPLETED, updated.state)
		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun updateEvaluationUseCase_whenEvaluationIsMissingRemotely_emitsNotFoundError() = runTest {
		val throwable = clientRequestException(
			statusCode = HttpStatusCode.NotFound,
			path = "/evaluations/v1/${DEFAULT_PENDING_EVALUATION.id}"
		)
		val evaluationRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			updateThrowable = throwable
		)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(validParams(evaluationId = DEFAULT_PENDING_EVALUATION.id)).test {
			assertEquals(UpdateEvaluationUseCaseError.NotFound, awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(1, evaluationRepository.updateCalls.size)
		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	@Test
	fun updateEvaluationUseCase_whenFailureIsUnhandled_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage corrupted")
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				updateThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(validParams(evaluationId = DEFAULT_PENDING_EVALUATION.id)).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	private fun createUseCase(
		evaluationRepository: RecordingEvaluationRepository,
		reportingRepository: RecordingReportingRepository
	) = UpdateEvaluationUseCase(
		evaluationRepository = evaluationRepository,
		reportingRepository = reportingRepository,
		exceptionHandler = UpdateEvaluationExceptionHandler()
	)

	private fun validParams(
		evaluationId: String,
		grade: Double? = 30.0,
		date: Long? = currentTimeMillis() - ONE_DAY_MILLIS
	) = UpdateEvaluationParams(
		evaluationId = evaluationId,
		attemptId = DEFAULT_PENDING_EVALUATION.attemptId,
		subjectCode = DEFAULT_PENDING_EVALUATION.subjectCode,
		termId = DEFAULT_PENDING_EVALUATION.termId,
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = grade,
		maxGrade = 35.0,
		date = date,
		type = EvaluationType.TEST
	)
}

private const val ONE_DAY_MILLIS = 86_400_000L
