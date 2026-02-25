package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AddEvaluationUseCaseTest {
	@Test
	fun execute_withValidParams_generatesReferenceAndDelegatesToRepository() = runBlocking {
		val repository = FakeEvaluationRepository()
		val useCase = AddEvaluationUseCase(
			evaluationRepository = repository,
			identifierRepository = FixedIdentifierRepository("generated-ref-1"),
			paramsValidator = AddEvaluationParamsValidator(),
			exceptionHandler = AddEvaluationExceptionHandler(FakeReportingGateway())
		)

		val states = useCase.execute(
			AddEvaluationParams(
				subjectId = "subject-1",
				subjectCode = "MA1111",
				quarterId = "quarter-1",
				type = EvaluationType.QUIZ,
				date = 1_735_700_000_000L,
				grade = 17.5,
				maxGrade = 20.0
			)
		).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, AddEvaluationUseCaseError>>(states[0])
		assertIs<UseCaseState.Data<Unit, AddEvaluationUseCaseError>>(states[1])

		val added = checkNotNull(repository.lastAdd)
		assertEquals("generated-ref-1", added.reference)
		assertEquals("subject-1", added.subjectId)
		assertEquals("MA1111", added.subjectCode)
		assertEquals("quarter-1", added.quarterId)
		assertEquals(EvaluationType.QUIZ, added.type)
		assertEquals(17.5, added.grade)
		assertEquals(20.0, added.maxGrade)
	}

	@Test
	fun execute_withMissingSubject_emitsSubjectMissedError() = runBlocking {
		val repository = FakeEvaluationRepository()
		val useCase = AddEvaluationUseCase(
			evaluationRepository = repository,
			identifierRepository = FixedIdentifierRepository("generated-ref-2"),
			paramsValidator = AddEvaluationParamsValidator(),
			exceptionHandler = AddEvaluationExceptionHandler(FakeReportingGateway())
		)

		val states = useCase.execute(
			AddEvaluationParams(
				subjectId = null,
				subjectCode = "MA1111",
				quarterId = "quarter-1",
				type = EvaluationType.TEST,
				date = null,
				grade = null,
				maxGrade = 20.0
			)
		).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, AddEvaluationUseCaseError>>(states[0])
		val error = assertIs<UseCaseState.Error<Unit, AddEvaluationUseCaseError>>(states[1])
		assertEquals(AddEvaluationUseCaseError.SubjectMissed, error.error)
		assertNull(repository.lastAdd)
	}
}

private class FakeEvaluationRepository : EvaluationRepository {
	var lastAdd: EvaluationAdd? = null

	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> = emptyFlow()

	override suspend fun getEvaluation(eid: String): Evaluation? = null

	override suspend fun addEvaluation(add: EvaluationAdd) {
		lastAdd = add
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) = Unit

	override suspend fun removeEvaluation(remove: EvaluationRemove) = Unit

	override suspend fun getAvailableSubjects(): List<Subject> = emptyList()
}

private class FixedIdentifierRepository(
	private val fixedId: String
) : IdentifierRepository {
	override fun generateRandomIdentifier(): String = fixedId
}

private class FakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
