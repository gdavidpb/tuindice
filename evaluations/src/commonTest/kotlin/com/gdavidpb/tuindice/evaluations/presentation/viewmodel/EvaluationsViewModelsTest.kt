package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationFilterLabelsRepository
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
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class EvaluationsViewModelsTest {
	@Test
	fun evaluationsViewModel_loadAndFilterFlow_updatesStateAndEffects() = runBlocking {
		val repository = EvaluationsViewModelFakeEvaluationRepository()
		val viewModel = createEvaluationsViewModel(repository)
		val effects = mutableListOf<Evaluations.Effect>()
		val effectJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.effect.collect { effects += it }
		}
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.state.collect()
		}

		try {
			waitUntil("evaluations action subscribed") { viewModel.action.subscriptionCount.value > 0 }
			waitUntil("evaluations effect subscribed") { viewModel.effect.subscriptionCount.value > 0 }

			viewModel.loadEvaluationsAction()
			waitUntil("evaluations content loaded") { viewModel.state.value is Evaluations.State.Content }

			val loaded = assertIs<Evaluations.State.Content>(viewModel.state.value)
			assertEquals(1, loaded.originalEvaluations.size)
			assertEquals("evaluation-1", loaded.originalEvaluations.single().id)

			val filter = EvaluationSubjectFilter("MA1111")
			viewModel.toggleFilterAction(filter = filter, isChecked = true)
			waitUntil("evaluations filter checked") {
				val state = viewModel.state.value
				state is Evaluations.State.Content && state.activeFilters.contains(filter)
			}

			viewModel.clearFiltersAction()
			waitUntil("evaluations filters cleared") {
				val state = viewModel.state.value
				state is Evaluations.State.Content && state.activeFilters.isEmpty()
			}

			viewModel.addEvaluationAction()
			waitUntil("evaluations navigate add") { effects.any { it is Evaluations.Effect.NavigateToAddEvaluation } }

			viewModel.showEvaluationGradeDialogAction("evaluation-1")
			waitUntil("evaluations show grade picker") { effects.any { it is Evaluations.Effect.NavigateToGradePickerDialog } }
			val gradeDialog = effects.filterIsInstance<Evaluations.Effect.NavigateToGradePickerDialog>().last()
			assertEquals("evaluation-1", gradeDialog.evaluationId)
			assertEquals(18.5, gradeDialog.grade)
			assertEquals(20.0, gradeDialog.maxGrade)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	@Test
	fun evaluationViewModel_loadAndAddFlow_updatesStateAndEffects() = runBlocking {
		val repository = EvaluationsViewModelFakeEvaluationRepository()
		val viewModel = createEvaluationViewModel(repository)
		val effects = mutableListOf<EvaluationContract.Effect>()
		val effectJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.effect.collect { effects += it }
		}
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.state.collect()
		}

		try {
			waitUntil("evaluation action subscribed") { viewModel.action.subscriptionCount.value > 0 }
			waitUntil("evaluation effect subscribed") { viewModel.effect.subscriptionCount.value > 0 }

			viewModel.loadAvailableSubjectsAction()
			waitUntil("evaluation content loaded") { viewModel.state.value is EvaluationContract.State.Content }

			val subject = repository.availableSubjects.first()
			viewModel.setSubjectAction(subject)
			waitUntil("evaluation subject set") {
				val state = viewModel.state.value
				state is EvaluationContract.State.Content && state.selectedSubject == subject
			}

			viewModel.setTypeAction(EvaluationType.QUIZ)
			waitUntil("evaluation type set") {
				val state = viewModel.state.value
				state is EvaluationContract.State.Content && state.type == EvaluationType.QUIZ
			}

			viewModel.setMaxGradeAction(20.0)
			waitUntil("evaluation max grade set") {
				val state = viewModel.state.value
				state is EvaluationContract.State.Content && state.maxGrade == 20.0
			}

			viewModel.setGradeAction(18.5)

			waitUntil("evaluation form updated") {
				val state = viewModel.state.value
				state is EvaluationContract.State.Content &&
					state.selectedSubject == subject &&
					state.type == EvaluationType.QUIZ &&
					state.maxGrade == 20.0 &&
					state.grade == 18.5
			}

			viewModel.clickGradeAction(grade = 18.5, maxGrade = 20.0)
			waitUntil("evaluation navigate grade picker") {
				effects.any { it is EvaluationContract.Effect.NavigateToGradePickerDialog }
			}

			viewModel.clickAddEvaluationAction(
				subject = subject,
				type = EvaluationType.QUIZ,
				date = 1_700_000_000_000L,
				grade = 18.5,
				maxGrade = 20.0
			)

			waitUntil("evaluation navigate evaluations") {
				effects.any { it is EvaluationContract.Effect.NavigateToEvaluations }
			}
			assertEquals(1, repository.addCalls)
			val snackBar = effects.filterIsInstance<EvaluationContract.Effect.ShowSnackBar>().last()
			assertEquals("Evaluation added", snackBar.message)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createEvaluationsViewModel(
		repository: EvaluationsViewModelFakeEvaluationRepository
	): EvaluationsViewModel {
		return EvaluationsViewModel(
			loadEvaluationsActionProcessor = LoadEvaluationsActionProcessor(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					quarterRepository = EvaluationsViewModelFakeQuarterRepository(),
					evaluationRepository = repository,
					filterLabelsProvider = EvaluationsViewModelFakeFilterLabelsProvider(),
					exceptionHandler = GetEvaluationsExceptionHandler(
						reportingRepository = EvaluationsViewModelFakeReportingGateway
					)
				),
				textProvider = EvaluationsViewModelFakeTextProvider
			),
			checkEvaluationFilterActionProcessor = CheckEvaluationFilterActionProcessor(),
			uncheckEvaluationFilterActionProcessor = UncheckEvaluationFilterActionProcessor(),
			clearEvaluationFiltersActionProcessor = ClearEvaluationFiltersActionProcessor(),
			openAddEvaluationActionProcessor = OpenAddEvaluationActionProcessor(),
			pickEvaluationGradeActionProcessor = PickEvaluationGradeActionProcessor(
				getEvaluationUseCase = GetEvaluationUseCase(repository),
				textProvider = EvaluationsViewModelFakeTextProvider
			),
			setEvaluationGradeActionProcessor = SetEvaluationGradeActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(repository),
				textProvider = EvaluationsViewModelFakeTextProvider
			),
			openEvaluationActionProcessor = OpenEvaluationActionProcessor(),
			removeEvaluationActionProcessor = RemoveEvaluationActionProcessor(
				removeEvaluationUseCase = RemoveEvaluationUseCase(repository),
				textProvider = EvaluationsViewModelFakeTextProvider
			)
		)
	}

	private fun createEvaluationViewModel(
		repository: EvaluationsViewModelFakeEvaluationRepository
	): EvaluationViewModel {
		return EvaluationViewModel(
			loadAvailableSubjectsActionProcessor = LoadAvailableSubjectsActionProcessor(
				getAvailableSubjectsUseCase = GetAvailableSubjectsUseCase(repository)
			),
			loadEvaluationActionProcessor = LoadEvaluationActionProcessor(
				getEvaluationAndAvailableSubjectsUseCase = GetEvaluationAndAvailableSubjectsUseCase(repository)
			),
			addEvaluationActionProcessor = AddEvaluationActionProcessor(
				addEvaluationUseCase = AddEvaluationUseCase(
					evaluationRepository = repository,
					identifierRepository = EvaluationsViewModelFixedIdentifierRepository("generated-reference"),
					paramsValidator = AddEvaluationParamsValidator(),
					exceptionHandler = AddEvaluationExceptionHandler(
						reportingRepository = EvaluationsViewModelFakeReportingGateway
					)
				),
				textProvider = EvaluationsViewModelFakeTextProvider
			),
			editEvaluationActionProcessor = EditEvaluationActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(repository),
				textProvider = EvaluationsViewModelFakeTextProvider
			),
			pickGradeActionProcessor = PickGradeActionProcessor(),
			pickMaxGradeActionProcessor = PickMaxGradeActionProcessor(),
			setSubjectActionProcessor = SetSubjectActionProcessor(),
			setTypeActionProcessor = SetTypeActionProcessor(),
			setDateActionProcessor = SetDateActionProcessor(),
			setGradeActionProcessor = SetGradeActionProcessor(),
			setMaxGradeActionProcessor = SetMaxGradeActionProcessor()
		)
	}

	private suspend fun waitUntil(
		label: String,
		timeoutMs: Long = 5_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout: $label")
	}
}

private class EvaluationsViewModelFakeEvaluationRepository : EvaluationRepository {
	var evaluations: MutableList<Evaluation> = mutableListOf(
		Evaluation(
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
	)
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
		evaluations = evaluations.filterNot { it.id == remove.id }.toMutableList()
	}

	override suspend fun getAvailableSubjects(): List<Subject> = availableSubjects
}

private class EvaluationsViewModelFakeQuarterRepository : QuarterRepository {
	override suspend fun getQuartersFlow(): Flow<List<Quarter>> = flowOf(emptyList())
	override suspend fun getQuarters(): List<Quarter> = emptyList()
	override suspend fun removeQuarter(remove: QuarterRemove) = Unit
	override suspend fun setSubjectGrade(set: SubjectGradeSet) = Unit
}

private class EvaluationsViewModelFakeFilterLabelsProvider : EvaluationFilterLabelsRepository {
	override fun pending(): String = "Pending"
	override fun completed(): String = "Completed"
	override fun noGrade(): String = "No grade"
	override fun date(date: Long?): String = date?.toString() ?: "No date"
}

private object EvaluationsViewModelFakeTextProvider : EvaluationTextProvider {
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

private class EvaluationsViewModelFixedIdentifierRepository(
	private val fixedId: String
) : IdentifierRepository {
	override fun generateRandomIdentifier(): String = fixedId
}

private object EvaluationsViewModelFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
