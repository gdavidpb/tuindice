package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
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
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = { maxGrade ->
					requestedMaxGrade = maxGrade
				},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.clickMaxGradeAction(20.0)
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
		var requestedGrade: Double? = null
		var requestedMaxGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = {},
				onNavigateToGradePickerDialog = { grade, maxGrade ->
					requestedGrade = grade
					requestedMaxGrade = maxGrade
				},
				onNavigateToMaxGradePickerDialog = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.clickGradeAction(17.5, 20.0)
		}

		waitUntil(timeoutMillis = 2_000) {
			requestedGrade != null && requestedMaxGrade != null
		}

		assertEquals(17.5, requestedGrade)
		assertEquals(20.0, requestedMaxGrade)
	}

	@Test
	fun when_gradeActionTriggeredWithNullValues_then_navigatesToGradeDialogWithNullPayload() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var gradeDialogRequested = false
		var requestedGrade: Double? = 0.0
		var requestedMaxGrade: Double? = 0.0

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = null,
				onNavigateToEvaluations = {},
				onNavigateToGradePickerDialog = { grade, maxGrade ->
					gradeDialogRequested = true
					requestedGrade = grade
					requestedMaxGrade = maxGrade
				},
				onNavigateToMaxGradePickerDialog = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.clickGradeAction(grade = null, maxGrade = null)
		}

		waitUntil(timeoutMillis = 2_000) {
			gradeDialogRequested
		}

		assertEquals(null, requestedGrade)
		assertEquals(null, requestedMaxGrade)
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
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = {},
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.clickAddEvaluationAction(
				subject = DEFAULT_EVALUATION_SUBJECT,
				type = DEFAULT_PENDING_EVALUATION.type,
				scheduleMode = DEFAULT_PENDING_EVALUATION.scheduleMode,
				date = DEFAULT_PENDING_EVALUATION.date,
				grade = DEFAULT_PENDING_EVALUATION.grade,
				maxGrade = DEFAULT_PENDING_EVALUATION.maxGrade
			)
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
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = {},
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
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = {},
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.clickEditEvaluationAction(
				evaluationId = DEFAULT_PENDING_EVALUATION.id,
				subject = DEFAULT_EVALUATION_SUBJECT,
				type = DEFAULT_PENDING_EVALUATION.type,
				scheduleMode = DEFAULT_PENDING_EVALUATION.scheduleMode,
				date = DEFAULT_PENDING_EVALUATION.date,
				grade = DEFAULT_PENDING_EVALUATION.grade,
				maxGrade = DEFAULT_PENDING_EVALUATION.maxGrade
			)
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
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = {},
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
			navigateCalls > 0 && snackBars.isNotEmpty()
		}

		assertEquals(1, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_maxGradeChipTappedFromUiInEditMode_then_navigatesToMaxGradeDialogWithCurrentValue() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var requestedMaxGrade: Double? = null
		val snackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = DEFAULT_PENDING_EVALUATION.id,
				onNavigateToEvaluations = {},
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = { maxGrade ->
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

		assertEquals(DEFAULT_PENDING_EVALUATION.maxGrade, requestedMaxGrade)
		assertTrue(snackBars.isEmpty())
	}

	@Test
	fun when_gradeChipTappedFromUiForOverdueEvaluation_then_navigatesToGradeDialogWithCurrentValues() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var requestedGrade: Double? = null
		var requestedMaxGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationRoute(
				evaluationId = DEFAULT_COMPLETED_EVALUATION.id,
				onNavigateToEvaluations = {},
				onNavigateToGradePickerDialog = { grade, maxGrade ->
					requestedGrade = grade
					requestedMaxGrade = maxGrade
				},
				onNavigateToMaxGradePickerDialog = {},
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

		assertEquals(DEFAULT_COMPLETED_EVALUATION.grade, requestedGrade)
		assertEquals(DEFAULT_COMPLETED_EVALUATION.maxGrade, requestedMaxGrade)
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
				onNavigateToGradePickerDialog = { _, _ -> },
				onNavigateToMaxGradePickerDialog = {},
				showSnackBar = { message -> snackBars += message },
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.clickAddEvaluationAction(
				subject = DEFAULT_EVALUATION_SUBJECT,
				type = DEFAULT_PENDING_EVALUATION.type,
				scheduleMode = DEFAULT_PENDING_EVALUATION.scheduleMode,
				date = DEFAULT_PENDING_EVALUATION.date,
				grade = DEFAULT_PENDING_EVALUATION.grade,
				maxGrade = null
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	private fun createViewModel(): EvaluationViewModel {
		val repository = RecordingEvaluationRepository(
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)

		return EvaluationViewModel(
			loadAvailableSubjectsActionProcessor = LoadAvailableSubjectsActionProcessor(
				getAvailableSubjectsUseCase = GetAvailableSubjectsUseCase(repository)
			),
			loadEvaluationActionProcessor = LoadEvaluationActionProcessor(
				getEvaluationAndAvailableSubjectsUseCase = GetEvaluationAndAvailableSubjectsUseCase(
					repository
				)
			),
			addEvaluationActionProcessor = AddEvaluationActionProcessor(
				addEvaluationUseCase = AddEvaluationUseCase(
					evaluationRepository = repository,
					identifierRepository = FakeIdentifierRepository(),
					paramsValidator = AddEvaluationParamsValidator(),
					exceptionHandler = AddEvaluationExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			editEvaluationActionProcessor = EditEvaluationActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					exceptionHandler = UpdateEvaluationExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
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
}
