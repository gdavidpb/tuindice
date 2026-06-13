package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class EvaluationsStateMachineContractTest {
	@Test
	fun listMachine_coversAlphabet_andStatesAreReachable() {
		val machine = createListViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			Evaluations.Action::class,
			EvaluationsInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Evaluations.State.Idle::class
		)

		assertMachineCoversEffects(machine, Evaluations.Effect::class)
	}

	@Test
	fun editorMachine_coversAlphabet_andStatesAreReachable() {
		val machine = createEditorViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			Evaluation.Action::class,
			EvaluationInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Evaluation.State.Loading::class
		)

		assertMachineCoversEffects(machine, Evaluation.Effect::class)
	}

	@Test
	fun machines_exportDeclaredTransitionsToMermaid() {
		val listDiagram = createListViewModel().exportMachineToMermaid()
		val editorDiagram = createEditorViewModel().exportMachineToMermaid()

		// Captured from test output to publish the generated diagrams as docs artifacts.
		println(listDiagram)
		println(editorDiagram)

		val expectedListFragments = listOf(
			"idle",
			"loading",
			"content",
			"empty",
			"no_attempts",
			"failed",
			"LoadEvaluations",
			"SelectWeek",
			"EvaluationsContentObserved",
			"EvaluationsWaitingObserved",
			"AddEvaluation / NavigateToAddEvaluation",
			"GradePickerLoaded / NavigateToGradePickerDialog",
			"EvaluationRemoved / ShowSnackBar"
		)

		for (fragment in expectedListFragments) {
			assertTrue(
				listDiagram.contains(fragment),
				"Expected list Mermaid export to mention '$fragment':\n$listDiagram"
			)
		}

		val expectedEditorFragments = listOf(
			"loading",
			"content",
			"failed",
			"LoadAvailableAttempts",
			"EditorContentLoaded",
			"SetAttempt",
			"ClickSubmitEvaluation",
			"SubmitSucceeded / ShowSnackBar · NavigateToEvaluations"
		)

		for (fragment in expectedEditorFragments) {
			assertTrue(
				editorDiagram.contains(fragment),
				"Expected editor Mermaid export to mention '$fragment':\n$editorDiagram"
			)
		}
	}

	@Test
	fun listMachine_survivesSeededRandomWalk() = runTest {
		val repository = RecordingEvaluationRepository()
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = EvaluationsMachine(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = repository,
				recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
				reportingRepository = reportingRepository
			),
			updateEvaluationsUseCase = UpdateEvaluationsUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository
			),
			getEvaluationUseCase = GetEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository
			),
			updateEvaluationUseCase = UpdateEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = UpdateEvaluationExceptionHandler()
			),
			removeEvaluationUseCase = RemoveEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = RemoveEvaluationExceptionHandler()
			)
		)

		val weekKey = EvaluationsWeekKey.Academic(weekNumber = 1)
		val weekItem = EvaluationsWeekItem(
			key = weekKey,
			labelText = "Semana 1",
			days = emptyList()
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Evaluations.Action.LoadEvaluations,
				Evaluations.Action.RefreshEvaluations,
				Evaluations.Action.SelectWeek(weekKey = weekKey),
				Evaluations.Action.AddEvaluation,
				Evaluations.Action.ShowEvaluationGradeDialog(
					evaluationId = "evaluation-1",
					evaluationName = "Parcial 1",
					subjectCode = "CI2125"
				),
				Evaluations.Action.SetEvaluationGrade(
					evaluationId = "evaluation-1",
					grade = 15.0
				),
				Evaluations.Action.EditEvaluation(evaluationId = "evaluation-1"),
				Evaluations.Action.RemoveEvaluation(evaluationId = "evaluation-1"),
				EvaluationsInternalEvent.EvaluationsWaitingObserved,
				EvaluationsInternalEvent.EvaluationsRecordDataUnavailableObserved,
				EvaluationsInternalEvent.EvaluationsNoAttemptsObserved,
				EvaluationsInternalEvent.EvaluationsContentObserved(
					weekItems = listOf(weekItem),
					defaultWeekKey = weekKey,
					evaluationWeekGroups = listOf(
						EvaluationsWeekGroupItem(
							key = weekKey,
							title = "Semana 1",
							groups = emptyList()
						)
					)
				),
				EvaluationsInternalEvent.EvaluationsEmptyObserved,
				EvaluationsInternalEvent.EvaluationsObservationFailed,
				EvaluationsInternalEvent.EvaluationsRefreshStarted,
				EvaluationsInternalEvent.EvaluationsRefreshFailed,
				EvaluationsInternalEvent.GradePickerLoaded(
					evaluationId = "evaluation-1",
					evaluationName = "Parcial 1",
					subjectCode = "CI2125",
					grade = 15.0,
					maxGrade = 20.0
				),
				EvaluationsInternalEvent.GradePickerLoadFailed(message = "No se pudo abrir"),
				EvaluationsInternalEvent.EvaluationGradeSaved(message = "Nota guardada"),
				EvaluationsInternalEvent.EvaluationGradeSaveFailed(message = "No se pudo guardar"),
				EvaluationsInternalEvent.EvaluationRemoved(message = "Evaluación eliminada"),
				EvaluationsInternalEvent.EvaluationRemoveFailed(message = "No se pudo eliminar")
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	@Test
	fun editorMachine_survivesSeededRandomWalk() = runTest {
		val repository = RecordingEvaluationRepository()
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = EvaluationMachine(
			getAvailableAttemptsUseCase = GetAvailableAttemptsUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository
			),
			getEvaluationAndAvailableAttemptsUseCase = GetEvaluationAndAvailableAttemptsUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository
			),
			addEvaluationUseCase = AddEvaluationUseCase(
				evaluationRepository = repository,
				identifierRepository = FakeIdentifierRepository(),
				reportingRepository = reportingRepository,
				paramsValidator = AddEvaluationParamsValidator(),
				exceptionHandler = AddEvaluationExceptionHandler()
			),
			updateEvaluationUseCase = UpdateEvaluationUseCase(
				evaluationRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = UpdateEvaluationExceptionHandler()
			)
		)

		val attempt = EditableAttemptDescriptor(
			id = "attempt-1",
			termId = "term-1",
			code = "CI2125",
			name = "Algoritmos y Estructuras I",
			credits = 4,
			grade = 15
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Evaluation.Action.LoadAvailableAttempts,
				Evaluation.Action.LoadEvaluation(evaluationId = "evaluation-1"),
				Evaluation.Action.SetAttempt(attempt = attempt),
				Evaluation.Action.SetType(type = EvaluationType.QUIZ),
				Evaluation.Action.SetDate(date = 1767225600000L),
				Evaluation.Action.SetGrade(grade = 15.0),
				Evaluation.Action.SetMaxGrade(maxGrade = 20.0),
				Evaluation.Action.ClickGrade(
					evaluationName = "Parcial 1",
					subjectCode = "CI2125",
					grade = 15.0,
					maxGrade = 20.0
				),
				Evaluation.Action.ClickMaxGrade(
					evaluationName = "Parcial 1",
					subjectCode = "CI2125",
					maxGrade = 20.0
				),
				Evaluation.Action.ClickSubmitEvaluation,
				EvaluationInternalEvent.EditorLoadStarted,
				EvaluationInternalEvent.EditorContentLoaded(
					content = Evaluation.State.Content()
				),
				EvaluationInternalEvent.EditorLoadFailed,
				EvaluationInternalEvent.SubmitStarted,
				EvaluationInternalEvent.SubmitSucceeded(message = "Evaluación guardada"),
				EvaluationInternalEvent.SubmitFailed(
					message = "No se pudo guardar",
					navigateBack = false
				)
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	private fun createListViewModel(): EvaluationsViewModel {
		val repository = RecordingEvaluationRepository()
		val reportingRepository = RecordingReportingRepository()

		return EvaluationsViewModel(
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					reportingRepository = reportingRepository
				),
				updateEvaluationsUseCase = UpdateEvaluationsUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository
				),
				getEvaluationUseCase = GetEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository
				),
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = UpdateEvaluationExceptionHandler()
				),
				removeEvaluationUseCase = RemoveEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = RemoveEvaluationExceptionHandler()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}

	private fun createEditorViewModel(): EvaluationViewModel {
		val repository = RecordingEvaluationRepository()
		val reportingRepository = RecordingReportingRepository()

		return EvaluationViewModel(
			screenMachine = EvaluationMachine(
				getAvailableAttemptsUseCase = GetAvailableAttemptsUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository
				),
				getEvaluationAndAvailableAttemptsUseCase = GetEvaluationAndAvailableAttemptsUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository
				),
				addEvaluationUseCase = AddEvaluationUseCase(
					evaluationRepository = repository,
					identifierRepository = FakeIdentifierRepository(),
					reportingRepository = reportingRepository,
					paramsValidator = AddEvaluationParamsValidator(),
					exceptionHandler = AddEvaluationExceptionHandler()
				),
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = UpdateEvaluationExceptionHandler()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
