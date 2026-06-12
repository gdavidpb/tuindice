package com.gdavidpb.tuindice.evaluations.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
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
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test
import kotlin.test.assertTrue

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
