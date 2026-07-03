package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddEvaluationUseCaseTest {
	@Test
	fun addEvaluationUseCase_whenParamsAreValid_persistsEvaluationWithGeneratedReference() = runTest {
		val evaluationRepository = RecordingEvaluationRepository(initialEvaluations = emptyList())
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository,
			generatedIdentifier = GENERATED_REFERENCE
		)
		val date = currentTimeMillis() + ONE_DAY_MILLIS
		val params = validParams(date = date)

		useCase.execute(params).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(
			EvaluationAdd(
				reference = GENERATED_REFERENCE,
				attemptId = DEFAULT_EVALUATION_SUBJECT.id,
				subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
				termId = DEFAULT_EVALUATION_SUBJECT.termId,
				type = EvaluationType.TEST,
				scheduleMode = EvaluationScheduleMode.DATED,
				date = date,
				grade = null,
				maxGrade = 20.0
			),
			evaluationRepository.addCalls.single()
		)
		assertNotNull(evaluationRepository.getEvaluation(GENERATED_REFERENCE))
		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun addEvaluationUseCase_whenAttemptIsMissing_emitsAttemptMissedWithoutPersisting() = runTest {
		val evaluationRepository = RecordingEvaluationRepository(initialEvaluations = emptyList())
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(validParams(attemptId = null)).test {
			val error = awaitLoadingThenError(this)
			assertEquals(AddEvaluationUseCaseError.AttemptMissed, error.error)
			awaitComplete()
		}

		assertTrue(evaluationRepository.addCalls.isEmpty())
		assertIs<AddEvaluationIllegalArgumentException>(reportingRepository.exceptions.single())
	}

	@Test
	fun addEvaluationUseCase_whenAttemptIsBlank_emitsAttemptMissedError() = runTest {
		val useCase = createUseCase(
			evaluationRepository = RecordingEvaluationRepository(initialEvaluations = emptyList()),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(validParams(attemptId = "   ")).test {
			assertEquals(AddEvaluationUseCaseError.AttemptMissed, awaitLoadingThenError(this).error)
			awaitComplete()
		}
	}

	@Test
	fun addEvaluationUseCase_whenMaxGradeIsNegative_emitsMaxGradeMissedError() = runTest {
		val useCase = createUseCase(
			evaluationRepository = RecordingEvaluationRepository(initialEvaluations = emptyList()),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(validParams(maxGrade = -1.0)).test {
			assertEquals(AddEvaluationUseCaseError.MaxGradeMissed, awaitLoadingThenError(this).error)
			awaitComplete()
		}
	}

	@Test
	fun addEvaluationUseCase_whenServerReportsPreconditionFailed_emitsAlreadyExistsError() = runTest {
		val throwable = clientRequestException(
			statusCode = HttpStatusCode.PreconditionFailed,
			path = "/evaluations/v1"
		)
		val evaluationRepository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			addThrowable = throwable
		)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(validParams()).test {
			assertEquals(AddEvaluationUseCaseError.AlreadyExists, awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(1, evaluationRepository.addCalls.size)
		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	@Test
	fun addEvaluationUseCase_whenFailureIsUnhandled_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage corrupted")
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				addThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(validParams()).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	private fun createUseCase(
		evaluationRepository: RecordingEvaluationRepository,
		reportingRepository: RecordingReportingRepository,
		generatedIdentifier: String = GENERATED_REFERENCE
	) = AddEvaluationUseCase(
		evaluationRepository = evaluationRepository,
		identifierRepository = FakeIdentifierRepository(identifier = generatedIdentifier),
		reportingRepository = reportingRepository,
		paramsValidator = AddEvaluationParamsValidator(),
		exceptionHandler = AddEvaluationExceptionHandler()
	)

	private fun validParams(
		attemptId: String? = DEFAULT_EVALUATION_SUBJECT.id,
		maxGrade: Double? = 20.0,
		date: Long? = currentTimeMillis() + ONE_DAY_MILLIS
	) = AddEvaluationParams(
		attemptId = attemptId,
		subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
		termId = DEFAULT_EVALUATION_SUBJECT.termId,
		type = EvaluationType.TEST,
		scheduleMode = EvaluationScheduleMode.DATED,
		date = date,
		grade = null,
		maxGrade = maxGrade
	)
}

private const val GENERATED_REFERENCE = "generated-evaluation-id"
private const val ONE_DAY_MILLIS = 86_400_000L
