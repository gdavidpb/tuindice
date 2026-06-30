package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationMachine
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationRouteUiTest {
	@Test
	fun when_maxGradeActionTriggered_then_navigatesToMaxGradeDialog() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var requestedMaxGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = {},
				onNavigateToGradePickerDialog = { _, _, _, _ -> },
				onNavigateToMaxGradePickerDialog = { _, _, maxGrade ->
					requestedMaxGrade = maxGrade
				},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Evaluation.State.Content
		}

		runOnIdle {
			viewModel.clickMaxGradeAction("Parcial 1", DEFAULT_EVALUATION_SUBJECT.code, 20.0)
		}

		waitUntil(timeoutMillis = 2_000) {
			requestedMaxGrade != null
		}

		assertEquals(20.0, requestedMaxGrade)
		assertTrue(snackBars.isEmpty())
	}

	@Test
	fun when_gradeActionTriggered_then_navigatesToGradeDialog() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var requestedEvaluationName = ""
		var requestedSubjectCode = ""
		var requestedGrade: Double? = null
		var requestedMaxGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = {},
				onNavigateToGradePickerDialog = { evaluationName, subjectCode, grade, maxGrade ->
					requestedEvaluationName = evaluationName
					requestedSubjectCode = subjectCode
					requestedGrade = grade
					requestedMaxGrade = maxGrade
				},
				onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Evaluation.State.Content
		}

		runOnIdle {
			viewModel.clickGradeAction("Parcial 1", DEFAULT_EVALUATION_SUBJECT.code, 17.5, 20.0)
		}

		waitUntil(timeoutMillis = 2_000) {
			requestedGrade != null && requestedMaxGrade != null
		}

		assertEquals("Parcial 1", requestedEvaluationName)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.code, requestedSubjectCode)
		assertEquals(17.5, requestedGrade)
		assertEquals(20.0, requestedMaxGrade)
	}

	@Test
	fun when_addEvaluationActionTriggered_then_showsSnackBarAndNavigatesToEvaluations() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigateCalls = 0

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = { navigateCalls++ },
					onNavigateToGradePickerDialog = { _, _, _, _ -> },
					onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Evaluation.State.Content
		}

		runOnIdle {
			viewModel.setAttemptAction(DEFAULT_EVALUATION_SUBJECT)
			viewModel.setTypeAction(DEFAULT_PENDING_EVALUATION.type)
			viewModel.setDateAction(DEFAULT_PENDING_EVALUATION.date)
			DEFAULT_PENDING_EVALUATION.grade?.let(viewModel::setGradeAction)
			viewModel.setMaxGradeAction(DEFAULT_PENDING_EVALUATION.maxGrade)
			viewModel.submitEvaluationAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigateCalls > 0 && snackBars.isNotEmpty()
		}

		assertEquals(1, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_doneFabTappedInAddModeWithIncompleteForm_then_showsSnackBarWithoutNavigation() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigateCalls = 0

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = { navigateCalls++ },
					onNavigateToGradePickerDialog = { _, _, _, _ -> },
					onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationDoneFab)
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab).performClick()

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_editEvaluationActionTriggered_then_showsSnackBarAndNavigatesToEvaluations() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigateCalls = 0

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = DEFAULT_PENDING_EVALUATION.id,
				onNavigateToEvaluations = { navigateCalls++ },
					onNavigateToGradePickerDialog = { _, _, _, _ -> },
					onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Evaluation.State.Content
		}

		// Edit mode loaded the evaluation into S; submitting reads the form from state.
		runOnIdle {
			viewModel.setTypeAction(EvaluationType.TEST)
			viewModel.submitEvaluationAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigateCalls > 0 && snackBars.isNotEmpty()
		}

		assertEquals(1, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_doneFabTappedInEditMode_then_showsSnackBarAndNavigatesToEvaluations() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigateCalls = 0

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = DEFAULT_PENDING_EVALUATION.id,
				onNavigateToEvaluations = { navigateCalls++ },
					onNavigateToGradePickerDialog = { _, _, _, _ -> },
					onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationDoneFab)
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab).assertIsNotEnabled()
		onNodeWithTag(EvaluationsUiTags.evaluationTypeChip(DEFAULT_PENDING_EVALUATION.type.name)).performClick()
		onNodeWithTag(EvaluationsUiTags.evaluationTypeChip(EvaluationType.TEST.name)).performClick()
		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab).assertIsEnabled()
		onNodeWithTag(EvaluationsUiTags.EvaluationDoneFab).performClick()

		waitUntil(timeoutMillis = 2_000) {
			navigateCalls > 0 && snackBars.isNotEmpty()
		}

		assertEquals(1, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_maxGradeChipTappedFromUiInEditMode_then_navigatesToMaxGradeDialogWithCurrentValue() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var requestedEvaluationName = ""
		var requestedSubjectCode = ""
		var requestedMaxGrade: Double? = null
		val snackBars = mutableListOf<SnackBarMessage>()

			setTuIndiceTestContent {
				EvaluationRoute(
					evaluationId = DEFAULT_PENDING_EVALUATION.id,
					onNavigateToEvaluations = {},
					onNavigateToGradePickerDialog = { _, _, _, _ -> },
					onNavigateToMaxGradePickerDialog = { evaluationName, subjectCode, maxGrade ->
						requestedEvaluationName = evaluationName
						requestedSubjectCode = subjectCode
						requestedMaxGrade = maxGrade
					},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationMaxGradeChip)
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationMaxGradeChip).performClick()

		waitUntil(timeoutMillis = 2_000) {
			requestedMaxGrade != null
		}

			assertEquals("Quiz 1", requestedEvaluationName)
			assertEquals(DEFAULT_EVALUATION_SUBJECT.code, requestedSubjectCode)
			assertEquals(DEFAULT_PENDING_EVALUATION.maxGrade, requestedMaxGrade)
		assertTrue(snackBars.isEmpty())
	}

	@Test
	fun when_gradeChipTappedFromUiForOverdueEvaluation_then_navigatesToGradeDialogWithCurrentValues() = runTuIndiceUiTest {
		val overdueEvaluation = DEFAULT_COMPLETED_EVALUATION.copy(date = 1_779_336_000_000L)
		val viewModel = createViewModel(
			repository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION, overdueEvaluation),
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
			)
		)
		var requestedEvaluationName = ""
		var requestedSubjectCode = ""
		var requestedGrade: Double? = null
		var requestedMaxGrade: Double? = null

			setTuIndiceTestContent {
				EvaluationRoute(
					evaluationId = overdueEvaluation.id,
					onNavigateToEvaluations = {},
					onNavigateToGradePickerDialog = { evaluationName, subjectCode, grade, maxGrade ->
						requestedEvaluationName = evaluationName
						requestedSubjectCode = subjectCode
						requestedGrade = grade
						requestedMaxGrade = maxGrade
					},
					onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationGradeChip)
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationGradeChip).performClick()

		waitUntil(timeoutMillis = 2_000) {
			requestedGrade != null && requestedMaxGrade != null
		}

			assertEquals("Parcial 1", requestedEvaluationName)
			assertEquals(SECOND_EVALUATION_SUBJECT.code, requestedSubjectCode)
			assertEquals(overdueEvaluation.grade, requestedGrade)
		assertEquals(overdueEvaluation.maxGrade, requestedMaxGrade)
	}

	@Test
	fun when_addEvaluationActionHasInvalidPayload_then_showsSnackBarWithoutNavigation() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigateCalls = 0

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = { navigateCalls++ },
					onNavigateToGradePickerDialog = { _, _, _, _ -> },
					onNavigateToMaxGradePickerDialog = { _, _, _ -> },
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Evaluation.State.Content
		}

		runOnIdle {
			viewModel.setAttemptAction(DEFAULT_EVALUATION_SUBJECT)
			viewModel.setTypeAction(DEFAULT_PENDING_EVALUATION.type)
			viewModel.setDateAction(DEFAULT_PENDING_EVALUATION.date)
			DEFAULT_PENDING_EVALUATION.grade?.let(viewModel::setGradeAction)
			viewModel.submitEvaluationAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	private fun createViewModel(
		repository: RecordingEvaluationRepository = RecordingEvaluationRepository(
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
	): EvaluationViewModel {
		return EvaluationViewModel(
			screenMachine = EvaluationMachine(
				getAvailableAttemptsUseCase = GetAvailableAttemptsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				getEvaluationAndAvailableAttemptsUseCase = GetEvaluationAndAvailableAttemptsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				addEvaluationUseCase = AddEvaluationUseCase(
					evaluationRepository = repository,
					identifierRepository = FakeIdentifierRepository(),
					reportingRepository = RecordingReportingRepository(),
					paramsValidator = AddEvaluationParamsValidator(),
					exceptionHandler = AddEvaluationExceptionHandler()
				),
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationExceptionHandler()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
