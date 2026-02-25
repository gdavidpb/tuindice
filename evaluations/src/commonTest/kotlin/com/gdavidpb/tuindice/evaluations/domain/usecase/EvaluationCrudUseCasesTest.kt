package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class EvaluationCrudUseCasesTest {
	@Test
	fun getAvailableSubjects_returnsRepositorySubjects() = runBlocking {
		val repository = CrudFakeEvaluationRepository(
			evaluations = listOf(sampleEvaluation(id = "e1"))
		).also { fake ->
			fake.availableSubjects = listOf(
				sampleSubject(id = "s1", code = "MA1111"),
				sampleSubject(id = "s2", code = "FI1111")
			)
		}
		val useCase = GetAvailableSubjectsUseCase(evaluationRepository = repository)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<List<Subject>, Nothing>>(states[0])
		val success = assertIs<UseCaseState.Data<List<Subject>, Nothing>>(states[1])
		assertEquals(listOf("MA1111", "FI1111"), success.value.map(Subject::code))
	}

	@Test
	fun getEvaluation_returnsMatchingEvaluation() = runBlocking {
		val repository = CrudFakeEvaluationRepository(
			evaluations = listOf(sampleEvaluation(id = "e1"), sampleEvaluation(id = "e2"))
		)
		val useCase = GetEvaluationUseCase(evaluationRepository = repository)

		val states = useCase.execute("e2").toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Evaluation?, EvaluationsUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<Evaluation?, EvaluationsUseCaseError>>(states[1])
		assertEquals("e2", success.value?.id)
	}

	@Test
	fun getEvaluation_whenMissing_returnsNullData() = runBlocking {
		val repository = CrudFakeEvaluationRepository(
			evaluations = listOf(sampleEvaluation(id = "e1"))
		)
		val useCase = GetEvaluationUseCase(evaluationRepository = repository)

		val states = useCase.execute("e-missing").toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Evaluation?, EvaluationsUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<Evaluation?, EvaluationsUseCaseError>>(states[1])
		assertNull(success.value)
	}

	@Test
	fun getEvaluationAndAvailableSubjects_returnsBothValues() = runBlocking {
		val repository = CrudFakeEvaluationRepository(
			evaluations = listOf(sampleEvaluation(id = "e-lookup"))
		).also { fake ->
			fake.availableSubjects = listOf(sampleSubject(id = "s-1", code = "MA1111"))
		}
		val useCase = GetEvaluationAndAvailableSubjectsUseCase(
			evaluationRepository = repository
		)

		val states = useCase.execute(GetEvaluationParams("e-lookup")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableSubjects, Nothing>>(states[0])
		val success = assertIs<UseCaseState.Data<com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableSubjects, Nothing>>(states[1])
		assertEquals("e-lookup", success.value.evaluation?.id)
		assertEquals(listOf("MA1111"), success.value.availableSubjects.map(Subject::code))
	}

	@Test
	fun updateEvaluation_mapsParamsAndDelegatesToRepository() = runBlocking {
		val repository = CrudFakeEvaluationRepository(
			evaluations = listOf(sampleEvaluation(id = "e1"))
		)
		val useCase = UpdateEvaluationUseCase(evaluationRepository = repository)

		val states = useCase.execute(
			UpdateEvaluationParams(
				evaluationId = "e1",
				subjectId = null,
				subjectCode = null,
				quarterId = null,
				grade = 18.5,
				maxGrade = 20.0,
				date = 1_735_700_000_000L,
				type = EvaluationType.ESSAY
			)
		).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, Nothing>>(states[0])
		assertIs<UseCaseState.Data<Unit, Nothing>>(states[1])
		assertEquals(
			EvaluationUpdate(
				id = "e1",
				grade = 18.5,
				maxGrade = 20.0,
				date = 1_735_700_000_000L,
				type = EvaluationType.ESSAY
			),
			repository.lastUpdate
		)
	}

	@Test
	fun removeEvaluation_mapsIdAndDelegatesToRepository() = runBlocking {
		val repository = CrudFakeEvaluationRepository(
			evaluations = listOf(sampleEvaluation(id = "e1"))
		)
		val useCase = RemoveEvaluationUseCase(evaluationRepository = repository)

		val states = useCase.execute("e-remove").toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, EvaluationsUseCaseError>>(states[0])
		assertIs<UseCaseState.Data<Unit, EvaluationsUseCaseError>>(states[1])
		assertEquals(EvaluationRemove(id = "e-remove"), repository.lastRemove)
	}
}

private class CrudFakeEvaluationRepository(
	private val evaluations: List<Evaluation>
) : EvaluationRepository {
	var availableSubjects: List<Subject> = emptyList()
	var lastUpdate: EvaluationUpdate? = null
	var lastRemove: EvaluationRemove? = null

	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> = flowOf(evaluations)

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(add: EvaluationAdd) = Unit

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		lastUpdate = update
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		lastRemove = remove
	}

	override suspend fun getAvailableSubjects(): List<Subject> = availableSubjects
}

private fun sampleEvaluation(id: String): Evaluation {
	return Evaluation(
		id = id,
		subjectId = "subject-$id",
		subjectCode = "MA1111",
		quarterId = "quarter-1",
		grade = 15.0,
		maxGrade = 20.0,
		date = 1_735_700_000_000L,
		type = EvaluationType.TEST,
		state = EvaluationState.PENDING
	)
}

private fun sampleSubject(id: String, code: String): Subject {
	return Subject(
		id = id,
		quarterId = "quarter-1",
		code = code,
		name = "Subject $code",
		credits = 4,
		grade = 0
	)
}
