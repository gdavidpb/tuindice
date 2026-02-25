package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationFilterLabelsRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetEvaluationsUseCaseTest {
	@Test
	fun execute_withoutFilters_returnsAllEvaluationsSortedByState() = runBlocking {
		val evaluationRepository = GetEvaluationsFakeEvaluationRepository(
			evaluations = listOf(
				evaluation(id = "e-completed", subjectCode = "MA1111", state = EvaluationState.COMPLETED),
				evaluation(id = "e-pending", subjectCode = "FI1111", state = EvaluationState.PENDING),
				evaluation(id = "e-continuous", subjectCode = "QU1111", state = EvaluationState.CONTINUOUS),
				evaluation(id = "e-overdue", subjectCode = "HI1111", state = EvaluationState.OVERDUE)
			)
		).also { repository ->
			repository.availableSubjects = listOf(
				Subject(
					id = "s1",
					quarterId = "q1",
					code = "MA1111",
					name = "Matematica",
					credits = 4,
					grade = 0
				)
			)
		}
		val useCase = GetEvaluationsUseCase(
			quarterRepository = FakeQuarterRepository(onGetQuarters = {}),
			evaluationRepository = evaluationRepository,
			filterLabelsProvider = FakeEvaluationFilterLabelsProvider(),
			exceptionHandler = GetEvaluationsExceptionHandler(GetEvaluationsFakeReportingGateway())
		)

		val states = useCase.execute(flowOf(emptyList())).toList()
		val success = assertIs<UseCaseState.Data<GetEvaluations, EvaluationsUseCaseError>>(states[1])

		assertEquals(
			listOf("e-pending", "e-overdue", "e-completed", "e-continuous"),
			success.value.originalEvaluations.map(Evaluation::id)
		)
		assertEquals(
			success.value.originalEvaluations.map(Evaluation::id),
			success.value.filteredEvaluations.map(Evaluation::id)
		)
	}

	@Test
	fun execute_withStateAndSubjectFilters_appliesOrWithinGroupAndAndAcrossGroups() = runBlocking {
		val evaluationRepository = GetEvaluationsFakeEvaluationRepository(
			evaluations = listOf(
				evaluation(id = "e1", subjectCode = "MA1111", state = EvaluationState.PENDING),
				evaluation(id = "e2", subjectCode = "FI1111", state = EvaluationState.PENDING),
				evaluation(id = "e3", subjectCode = "MA1111", state = EvaluationState.COMPLETED),
				evaluation(id = "e4", subjectCode = "HI1111", state = EvaluationState.PENDING)
			)
		).also { repository ->
			repository.availableSubjects = listOf(
				Subject(
					id = "s1",
					quarterId = "q1",
					code = "MA1111",
					name = "Matematica",
					credits = 4,
					grade = 0
				)
			)
		}
		val useCase = GetEvaluationsUseCase(
			quarterRepository = FakeQuarterRepository(onGetQuarters = {}),
			evaluationRepository = evaluationRepository,
			filterLabelsProvider = FakeEvaluationFilterLabelsProvider(),
			exceptionHandler = GetEvaluationsExceptionHandler(GetEvaluationsFakeReportingGateway())
		)
		val activeFilters = flowOf(
			listOf(
				EvaluationSubjectFilter("MA1111"),
				EvaluationSubjectFilter("FI1111"),
				EvaluationStateFilter(label = "Pending") { evaluation ->
					evaluation.state == EvaluationState.PENDING
				}
			)
		)

		val states = useCase.execute(activeFilters).toList()
		val success = assertIs<UseCaseState.Data<GetEvaluations, EvaluationsUseCaseError>>(states[1])

		assertEquals(listOf("e1", "e2"), success.value.filteredEvaluations.map(Evaluation::id))
	}

	@Test
	fun execute_whenSubjectsAreMissingInitially_refreshesQuartersAndAppliesFilters() = runBlocking {
		val evaluationRepository = GetEvaluationsFakeEvaluationRepository(
			evaluations = listOf(
				evaluation(id = "e1", subjectCode = "MA1111"),
				evaluation(id = "e2", subjectCode = "FI1111")
			)
		)
		val quarterRepository = FakeQuarterRepository {
			evaluationRepository.availableSubjects = listOf(
				Subject(
					id = "s1",
					quarterId = "q1",
					code = "MA1111",
					name = "Matematica",
					credits = 4,
					grade = 0
				)
			)
		}
		val useCase = GetEvaluationsUseCase(
			quarterRepository = quarterRepository,
			evaluationRepository = evaluationRepository,
			filterLabelsProvider = FakeEvaluationFilterLabelsProvider(),
			exceptionHandler = GetEvaluationsExceptionHandler(GetEvaluationsFakeReportingGateway())
		)

		val activeFilters: Flow<List<EvaluationFilter>> = flowOf(
			listOf(EvaluationSubjectFilter("MA1111"))
		)
		val states = useCase.execute(activeFilters).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<GetEvaluations, EvaluationsUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<GetEvaluations, EvaluationsUseCaseError>>(states[1])

		assertEquals(1, quarterRepository.getQuartersCalls)
		assertEquals(1, success.value.filteredEvaluations.size)
		assertEquals("e1", success.value.filteredEvaluations.single().id)
	}

	@Test
	fun execute_whenSubjectsRemainMissing_emitsNoSubjectsError() = runBlocking {
		val evaluationRepository = GetEvaluationsFakeEvaluationRepository(
			evaluations = emptyList()
		)
		val quarterRepository = FakeQuarterRepository(onGetQuarters = {})
		val useCase = GetEvaluationsUseCase(
			quarterRepository = quarterRepository,
			evaluationRepository = evaluationRepository,
			filterLabelsProvider = FakeEvaluationFilterLabelsProvider(),
			exceptionHandler = GetEvaluationsExceptionHandler(GetEvaluationsFakeReportingGateway())
		)

		val states = useCase.execute(flowOf(emptyList())).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<GetEvaluations, EvaluationsUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<GetEvaluations, EvaluationsUseCaseError>>(states[1])
		assertEquals(EvaluationsUseCaseError.NoSubjects, failure.error)
		assertEquals(1, quarterRepository.getQuartersCalls)
	}
}

private class GetEvaluationsFakeEvaluationRepository(
	private val evaluations: List<Evaluation>
) : EvaluationRepository {
	var availableSubjects: List<Subject> = emptyList()

	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> = flowOf(evaluations)

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return evaluations.firstOrNull { it.id == eid }
	}

	override suspend fun addEvaluation(add: com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd) = Unit

	override suspend fun updateEvaluation(update: EvaluationUpdate) = Unit

	override suspend fun removeEvaluation(remove: EvaluationRemove) = Unit

	override suspend fun getAvailableSubjects(): List<Subject> = availableSubjects
}

private class FakeQuarterRepository(
	private val onGetQuarters: () -> Unit
) : QuarterRepository {
	var getQuartersCalls: Int = 0

	override suspend fun getQuartersFlow(): Flow<List<Quarter>> = flowOf(emptyList())

	override suspend fun getQuarters(): List<Quarter> {
		getQuartersCalls++
		onGetQuarters()
		return emptyList()
	}

	override suspend fun removeQuarter(remove: QuarterRemove) = Unit

	override suspend fun setSubjectGrade(set: SubjectGradeSet) = Unit
}

private class FakeEvaluationFilterLabelsProvider : EvaluationFilterLabelsRepository {
	override fun pending(): String = "Pending"

	override fun completed(): String = "Completed"

	override fun noGrade(): String = "No grade"

	override fun date(date: Long?): String = date?.toString() ?: "Sin fecha"
}

private class GetEvaluationsFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun evaluation(
	id: String,
	subjectCode: String,
	state: EvaluationState = EvaluationState.PENDING
): Evaluation {
	return Evaluation(
		id = id,
		subjectId = "subject-$id",
		subjectCode = subjectCode,
		quarterId = "quarter-1",
		grade = null,
		maxGrade = 20.0,
		date = null,
		type = EvaluationType.TEST,
		state = state
	)
}
