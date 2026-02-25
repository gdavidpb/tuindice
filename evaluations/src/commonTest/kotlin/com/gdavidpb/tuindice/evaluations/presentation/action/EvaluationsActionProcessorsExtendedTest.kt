package com.gdavidpb.tuindice.evaluations.presentation.action

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationFilterLabelsProvider
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableSubjectsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation as EvaluationContract
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EvaluationsActionProcessorsExtendedTest {
	@Test
	fun loadEvaluationsActionProcessor_whenData_returnsContent() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				quarterRepository = FakeQuarterRepository(),
				evaluationRepository = repository,
				filterLabelsProvider = FakeFilterLabelsProvider(),
				exceptionHandler = GetEvaluationsExceptionHandler(FakeReportingGateway)
			),
			textProvider = FakeEvaluationTextProvider
		)

		val mutations = processor.process(
			action = Evaluations.Action.LoadEvaluations(flowOf(emptyList())),
			sideEffect = {}
		).toList()
		val finalState = applyEvaluationsMutations(
			initialState = Evaluations.State.Loading,
			mutations = mutations
		)

		val content = assertIs<Evaluations.State.Content>(finalState)
		assertEquals(1, content.originalEvaluations.size)
		assertEquals("evaluation-1", content.originalEvaluations.single().id)
	}

	@Test
	fun loadEvaluationsActionProcessor_whenNoSubjects_returnsNoSubjects() = runBlocking {
		val repository = FakeEvaluationRepository(
			evaluations = mutableListOf(),
			availableSubjects = emptyList()
		)
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				quarterRepository = FakeQuarterRepository(),
				evaluationRepository = repository,
				filterLabelsProvider = FakeFilterLabelsProvider(),
				exceptionHandler = GetEvaluationsExceptionHandler(FakeReportingGateway)
			),
			textProvider = FakeEvaluationTextProvider
		)

		val mutations = processor.process(
			action = Evaluations.Action.LoadEvaluations(flowOf(emptyList())),
			sideEffect = {}
		).toList()
		val finalState = applyEvaluationsMutations(
			initialState = Evaluations.State.Loading,
			mutations = mutations
		)

		assertEquals(Evaluations.State.NoSubjects, finalState)
	}

	@Test
	fun filterProcessors_updateActiveFilters() = runBlocking {
		val checkProcessor = CheckEvaluationFilterActionProcessor()
		val uncheckProcessor = UncheckEvaluationFilterActionProcessor()
		val clearProcessor = ClearEvaluationFiltersActionProcessor()
		val filter = EvaluationSubjectFilter("MA1111")
		val initialState = evaluationsContentState(activeFilters = emptyList())

		val checkedState = applyEvaluationsMutations(
			initialState,
			checkProcessor.process(
				action = Evaluations.Action.CheckEvaluationFilter(filter),
				sideEffect = {}
			).toList()
		)
		assertTrue((checkedState as Evaluations.State.Content).activeFilters.contains(filter))

		val uncheckedState = applyEvaluationsMutations(
			checkedState,
			uncheckProcessor.process(
				action = Evaluations.Action.UncheckEvaluationFilter(filter),
				sideEffect = {}
			).toList()
		)
		assertTrue((uncheckedState as Evaluations.State.Content).activeFilters.isEmpty())

		val preClearState = evaluationsContentState(activeFilters = listOf(filter))
		val clearedState = applyEvaluationsMutations(
			preClearState,
			clearProcessor.process(
				action = Evaluations.Action.ClearEvaluationFilters,
				sideEffect = {}
			).toList()
		)
		assertTrue((clearedState as Evaluations.State.Content).activeFilters.isEmpty())
	}

	@Test
	fun setEvaluationGradeActionProcessor_onSuccess_emitsUpdatedSnackBar() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = SetEvaluationGradeActionProcessor(
			updateEvaluationUseCase = UpdateEvaluationUseCase(repository),
			textProvider = FakeEvaluationTextProvider
		)
		val effects = mutableListOf<Evaluations.Effect>()

		processor.process(
			action = Evaluations.Action.SetEvaluationGrade(
				evaluationId = "evaluation-1",
				grade = 19.0
			),
			sideEffect = effects::add
		).toList()

		assertEquals(19.0, repository.evaluations.first().grade)
		val snackBar = assertIs<Evaluations.Effect.ShowSnackBar>(effects.single())
		assertEquals("Evaluation grade updated", snackBar.message)
	}

	@Test
	fun pickEvaluationGradeActionProcessor_whenEvaluationMissing_emitsDefaultError() = runBlocking {
		val repository = FakeEvaluationRepository(evaluations = mutableListOf())
		val processor = PickEvaluationGradeActionProcessor(
			getEvaluationUseCase = GetEvaluationUseCase(repository),
			textProvider = FakeEvaluationTextProvider
		)
		val effects = mutableListOf<Evaluations.Effect>()

		processor.process(
			action = Evaluations.Action.ShowEvaluationGradeDialog("missing"),
			sideEffect = effects::add
		).toList()

		val snackBar = assertIs<Evaluations.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
	}

	@Test
	fun removeEvaluationActionProcessor_onSuccess_emitsRemovedSnackBar() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = RemoveEvaluationActionProcessor(
			removeEvaluationUseCase = RemoveEvaluationUseCase(repository),
			textProvider = FakeEvaluationTextProvider
		)
		val effects = mutableListOf<Evaluations.Effect>()

		processor.process(
			action = Evaluations.Action.RemoveEvaluation("evaluation-1"),
			sideEffect = effects::add
		).toList()

		assertTrue(repository.evaluations.isEmpty())
		val snackBar = assertIs<Evaluations.Effect.ShowSnackBar>(effects.single())
		assertEquals("Evaluation removed", snackBar.message)
	}

	@Test
	fun loadAvailableSubjectsActionProcessor_returnsContent() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = LoadAvailableSubjectsActionProcessor(
			getAvailableSubjectsUseCase = GetAvailableSubjectsUseCase(repository)
		)

		val mutations = processor.process(
			action = EvaluationContract.Action.LoadAvailableSubjects,
			sideEffect = {}
		).toList()
		val finalState = applyEvaluationMutations(
			initialState = EvaluationContract.State.Loading,
			mutations = mutations
		)

		val content = assertIs<EvaluationContract.State.Content>(finalState)
		assertEquals(1, content.availableSubjects.size)
		assertEquals("MA1111", content.availableSubjects.single().code)
	}

	@Test
	fun loadEvaluationActionProcessor_returnsContentWithSelectedSubject() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = LoadEvaluationActionProcessor(
			getEvaluationAndAvailableSubjectsUseCase = GetEvaluationAndAvailableSubjectsUseCase(repository)
		)

		val mutations = processor.process(
			action = EvaluationContract.Action.LoadEvaluation("evaluation-1"),
			sideEffect = {}
		).toList()
		val finalState = applyEvaluationMutations(
			initialState = EvaluationContract.State.Loading,
			mutations = mutations
		)

		val content = assertIs<EvaluationContract.State.Content>(finalState)
		assertEquals("evaluation-1", content.evaluationId)
		assertEquals("MA1111", content.selectedSubject?.code)
		assertFalse(content.isOverdue)
	}

	@Test
	fun setSubjectAndSetDateAndSetMaxGradeProcessors_updateContent() = runBlocking {
		val setSubjectActionProcessor = SetSubjectActionProcessor()
		val setDateActionProcessor = SetDateActionProcessor()
		val setMaxGradeActionProcessor = SetMaxGradeActionProcessor()
		val subject = Subject(
			id = "subject-1",
			quarterId = "quarter-1",
			code = "MA1111",
			name = "Matematica",
			credits = 4,
			grade = 0
		)
		val initial = EvaluationContract.State.Content(
			availableSubjects = listOf(subject),
			grade = 19.0,
			maxGrade = 20.0
		)

		val withSubject = applyEvaluationMutations(
			initial,
			setSubjectActionProcessor.process(
				action = EvaluationContract.Action.SetSubject(subject),
				sideEffect = {}
			).toList()
		)
		assertEquals(subject, (withSubject as EvaluationContract.State.Content).selectedSubject)

		val withDate = applyEvaluationMutations(
			withSubject,
			setDateActionProcessor.process(
				action = EvaluationContract.Action.SetDate(4_102_444_800_000L),
				sideEffect = {}
			).toList()
		)
		assertFalse((withDate as EvaluationContract.State.Content).isOverdue)

		val withMax = applyEvaluationMutations(
			withDate,
			setMaxGradeActionProcessor.process(
				action = EvaluationContract.Action.SetMaxGrade(10.0),
				sideEffect = {}
			).toList()
		)
		val content = assertIs<EvaluationContract.State.Content>(withMax)
		assertEquals(10.0, content.maxGrade)
		assertEquals(10.0, content.grade)
	}

	@Test
	fun addEvaluationActionProcessor_whenSubjectMissing_emitsValidationSnackBar() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = AddEvaluationActionProcessor(
			addEvaluationUseCase = AddEvaluationUseCase(
				evaluationRepository = repository,
				identifierRepository = FixedIdentifierRepository("generated-reference"),
				paramsValidator = AddEvaluationParamsValidator(),
				exceptionHandler = AddEvaluationExceptionHandler(FakeReportingGateway)
			),
			textProvider = FakeEvaluationTextProvider
		)
		val effects = mutableListOf<EvaluationContract.Effect>()

		processor.process(
			action = EvaluationContract.Action.ClickAddEvaluation(
				subject = null,
				type = EvaluationType.QUIZ,
				date = null,
				grade = null,
				maxGrade = 20.0
			),
			sideEffect = effects::add
		).toList()

		assertEquals(0, repository.addCalls)
		val snackBar = assertIs<EvaluationContract.Effect.ShowSnackBar>(effects.single())
		assertEquals("Subject required", snackBar.message)
	}

	@Test
	fun editEvaluationActionProcessor_onSuccess_emitsUpdatedAndNavigate() = runBlocking {
		val repository = FakeEvaluationRepository()
		val processor = EditEvaluationActionProcessor(
			updateEvaluationUseCase = UpdateEvaluationUseCase(repository),
			textProvider = FakeEvaluationTextProvider
		)
		val effects = mutableListOf<EvaluationContract.Effect>()
		val subject = repository.availableSubjects.single()

		processor.process(
			action = EvaluationContract.Action.ClickEditEvaluation(
				evaluationId = "evaluation-1",
				subject = subject,
				type = EvaluationType.QUIZ,
				date = 1_700_000_000_000L,
				grade = 17.0,
				maxGrade = 20.0
			),
			sideEffect = effects::add
		).toList()

		assertEquals(17.0, repository.evaluations.single().grade)
		assertEquals(2, effects.size)
		val message = assertIs<EvaluationContract.Effect.ShowSnackBar>(effects[0])
		assertEquals("Evaluation updated", message.message)
		assertEquals(EvaluationContract.Effect.NavigateToEvaluations, effects[1])
	}

	private fun evaluationsContentState(activeFilters: List<EvaluationFilter>): Evaluations.State.Content {
		val evaluation = Evaluation(
			id = "evaluation-1",
			subjectId = "subject-1",
			subjectCode = "MA1111",
			quarterId = "quarter-1",
			grade = 18.5,
			maxGrade = 20.0,
			date = 1_700_000_000_000L,
			type = EvaluationType.TEST,
			state = EvaluationState.COMPLETED
		)
		return Evaluations.State.Content(
			originalEvaluations = listOf(evaluation),
			filteredEvaluations = listOf(evaluation),
			availableFilters = emptyList(),
			activeFilters = activeFilters
		)
	}

	private fun applyEvaluationsMutations(
		initialState: Evaluations.State,
		mutations: List<(Evaluations.State) -> Evaluations.State>
	): Evaluations.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}

	private fun applyEvaluationMutations(
		initialState: EvaluationContract.State,
		mutations: List<(EvaluationContract.State) -> EvaluationContract.State>
	): EvaluationContract.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private class FakeEvaluationRepository(
	var evaluations: MutableList<Evaluation> = mutableListOf(
		Evaluation(
			id = "evaluation-1",
			subjectId = "subject-1",
			subjectCode = "MA1111",
			quarterId = "quarter-1",
			grade = 18.5,
			maxGrade = 20.0,
			date = 4_102_444_800_000L,
			type = EvaluationType.TEST,
			state = EvaluationState.COMPLETED
		)
	),
	var availableSubjects: List<Subject> = listOf(
		Subject(
			id = "subject-1",
			quarterId = "quarter-1",
			code = "MA1111",
			name = "Matematica",
			credits = 4,
			grade = 0
		)
	)
) : EvaluationRepository {
	var addCalls: Int = 0

	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> = flowOf(evaluations.toList())

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return evaluations.firstOrNull { it.id == eid }
	}

	override suspend fun addEvaluation(add: EvaluationAdd) {
		addCalls++
		evaluations.add(
			Evaluation(
				id = add.reference,
				subjectId = add.subjectId,
				subjectCode = add.subjectCode,
				quarterId = add.quarterId,
				grade = add.grade,
				maxGrade = add.maxGrade,
				date = add.date,
				type = add.type,
				state = EvaluationState.PENDING
			)
		)
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		evaluations = evaluations.map { evaluation ->
			if (evaluation.id == update.id)
				evaluation.copy(
					grade = update.grade,
					maxGrade = update.maxGrade ?: evaluation.maxGrade,
					date = update.date,
					type = update.type ?: evaluation.type
				)
			else
				evaluation
		}.toMutableList()
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		evaluations = evaluations
			.filterNot { it.id == remove.id }
			.toMutableList()
	}

	override suspend fun getAvailableSubjects(): List<Subject> = availableSubjects
}

private class FakeQuarterRepository : QuarterRepository {
	override suspend fun getQuartersFlow(): Flow<List<Quarter>> = flowOf(emptyList())
	override suspend fun getQuarters(): List<Quarter> = emptyList()
	override suspend fun removeQuarter(remove: QuarterRemove) = Unit
	override suspend fun setSubjectGrade(set: SubjectGradeSet) = Unit
}

private class FakeFilterLabelsProvider : EvaluationFilterLabelsProvider {
	override fun pending(): String = "Pending"
	override fun completed(): String = "Completed"
	override fun noGrade(): String = "No grade"
	override fun date(date: Long?): String = date?.toString() ?: "No date"
}

private object FakeEvaluationTextProvider : EvaluationTextProvider {
	override fun defaultError(): String = "Default error"
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun timeout(): String = "Timeout"
	override fun evaluationAdded(): String = "Evaluation added"
	override fun evaluationUpdated(): String = "Evaluation updated"
	override fun evaluationRemoved(): String = "Evaluation removed"
	override fun evaluationGradeUpdated(): String = "Evaluation grade updated"
	override fun evaluationSubjectMissed(): String = "Subject required"
	override fun evaluationTypeMissed(): String = "Type required"
	override fun evaluationMaxGradeMissed(): String = "Max grade required"
}

private class FixedIdentifierRepository(
	private val fixedId: String
) : IdentifierRepository {
	override fun generateRandomIdentifier(): String = fixedId
}

private object FakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
