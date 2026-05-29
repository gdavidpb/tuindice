package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RefreshEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationsRouteUiTest {
	@Test
	fun when_addEvaluationActionTriggered_then_navigatesToAddScreen() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var addNavigationCalls = 0

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {
					addNavigationCalls++
				},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.addEvaluationAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			addNavigationCalls > 0
		}

		assertEquals(1, addNavigationCalls)
		assertTrue(snackBars.isEmpty())
	}

	@Test
	fun when_addFabTapped_then_navigatesToAddScreen() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var addNavigationCalls = 0

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {
					addNavigationCalls++
				},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationsAddFab)
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

		waitUntil(timeoutMillis = 2_000) {
			addNavigationCalls > 0
		}

		assertEquals(1, addNavigationCalls)
		assertTrue(snackBars.isEmpty())
	}

	@Test
	fun when_editEvaluationActionTriggered_then_navigatesToEvaluationScreen() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var navigatedEvaluationId = ""

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = { evaluationId ->
					navigatedEvaluationId = evaluationId
				},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.editEvaluationAction(DEFAULT_PENDING_EVALUATION.id)
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedEvaluationId.isNotEmpty()
		}

		assertEquals(DEFAULT_PENDING_EVALUATION.id, navigatedEvaluationId)
	}

	@Test
	fun when_showEvaluationGradeDialogActionTriggered_then_navigatesToGradePickerDialog() = runTuIndiceUiTest {
			val viewModel = createViewModel()
			var requestedEvaluationId = ""
			var requestedEvaluationName = ""
			var requestedSubjectCode = ""
			var requestedGrade: Double? = null
			var requestedMaxGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
					onNavigateToEvaluationGradePickerDialog = { evaluationId, evaluationName, subjectCode, grade, maxGrade ->
						requestedEvaluationId = evaluationId
						requestedEvaluationName = evaluationName
						requestedSubjectCode = subjectCode
						requestedGrade = grade
						requestedMaxGrade = maxGrade
					},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		runOnIdle {
				viewModel.showEvaluationGradeDialogAction(
					evaluationId = DEFAULT_COMPLETED_EVALUATION.id,
					evaluationName = "Parcial 1",
					subjectCode = SECOND_EVALUATION_SUBJECT.code
				)
			}

		waitUntil(timeoutMillis = 2_000) {
			requestedEvaluationId.isNotEmpty() &&
				requestedGrade != null &&
				requestedMaxGrade != null
			}

			assertEquals(DEFAULT_COMPLETED_EVALUATION.id, requestedEvaluationId)
			assertEquals("Parcial 1", requestedEvaluationName)
			assertEquals(SECOND_EVALUATION_SUBJECT.code, requestedSubjectCode)
			assertEquals(DEFAULT_COMPLETED_EVALUATION.grade, requestedGrade)
		assertEquals(DEFAULT_COMPLETED_EVALUATION.maxGrade, requestedMaxGrade)
	}

	@Test
	fun when_completedEvaluationGradeButtonTapped_then_navigatesToGradePickerDialog() = runTuIndiceUiTest {
			val viewModel = createViewModel()
			var requestedEvaluationId = ""
			var requestedEvaluationName = ""
			var requestedSubjectCode = ""
			var requestedGrade: Double? = null
			var requestedMaxGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
					onNavigateToEvaluationGradePickerDialog = { evaluationId, evaluationName, subjectCode, grade, maxGrade ->
						requestedEvaluationId = evaluationId
						requestedEvaluationName = evaluationName
						requestedSubjectCode = subjectCode
						requestedGrade = grade
						requestedMaxGrade = maxGrade
					},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		val completedEvaluationTag = EvaluationsUiTags.evaluationItemCard(DEFAULT_COMPLETED_EVALUATION.id)

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(completedEvaluationTag).fetchSemanticsNodes().isNotEmpty()
		}

		onAllNodesWithTag(EvaluationsUiTags.EvaluationGradeActionButton)[0].performClick()

		waitUntil(timeoutMillis = 2_000) {
			requestedEvaluationId.isNotEmpty() &&
				requestedGrade != null &&
				requestedMaxGrade != null
		}

			assertEquals(DEFAULT_COMPLETED_EVALUATION.id, requestedEvaluationId)
			assertEquals("Parcial 1", requestedEvaluationName)
			assertEquals(SECOND_EVALUATION_SUBJECT.code, requestedSubjectCode)
			assertEquals(DEFAULT_COMPLETED_EVALUATION.grade, requestedGrade)
		assertEquals(DEFAULT_COMPLETED_EVALUATION.maxGrade, requestedMaxGrade)
	}

	@Test
	fun when_showEvaluationGradeDialogActionHasUnknownId_then_showsSnackBarWithoutGradeNavigation() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var gradeNavigationCalls = 0

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ ->
					gradeNavigationCalls++
				},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

			runOnIdle {
				viewModel.showEvaluationGradeDialogAction(
					evaluationId = "missing-evaluation-id",
					evaluationName = "Parcial 1",
					subjectCode = SECOND_EVALUATION_SUBJECT.code
				)
			}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertEquals(0, gradeNavigationCalls)
		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_removeEvaluationActionTriggered_then_showsSnackBar() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.removeEvaluationAction(DEFAULT_PENDING_EVALUATION.id)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertTrue(snackBars.first().message.isNotBlank())
	}

	@Test
	fun when_routeStartsWithSuccessfulDataLoad_then_doesNotEmitNavigationOrSnackBarEffects() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var addNavigationCalls = 0
		var editNavigationCalls = 0
		var gradeNavigationCalls = 0
		val snackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = { addNavigationCalls++ },
				onNavigateToEvaluation = { editNavigationCalls++ },
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ ->
					gradeNavigationCalls++
				},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitForIdle()

		assertEquals(0, addNavigationCalls)
		assertEquals(0, editNavigationCalls)
		assertEquals(0, gradeNavigationCalls)
		assertTrue(snackBars.isEmpty())
	}

	@Test
	fun when_retryTappedAfterFailedLoad_then_routeRequestsEvaluationsAgain() = runTuIndiceUiTest {
		var evaluationsFlowCalls = 0
		val viewModel = createViewModel(
			repository = RecordingEvaluationRepository(
				evaluationsFlow = flow {
					evaluationsFlowCalls++
					throw IllegalStateException("boom")
				},
				refreshThrowable = IllegalStateException("refresh boom"),
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
			)
		)
		val snackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			evaluationsFlowCalls > 0
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(BaseUiTags.ErrorViewRetryButton)
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			evaluationsFlowCalls >= 2
		}

		assertTrue(evaluationsFlowCalls >= 2)
		assertTrue(snackBars.isEmpty())
	}

	private fun createViewModel(
		repository: EvaluationRepository = RecordingEvaluationRepository(
			evaluationsFlow = flowOf(
				listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
	): EvaluationsViewModel {

		return EvaluationsViewModel(
			loadEvaluationsActionProcessor = LoadEvaluationsActionProcessor(
					getEvaluationsUseCase = GetEvaluationsUseCase(
						evaluationRepository = repository,
						recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
						reportingRepository = RecordingReportingRepository()
					)
				),
			refreshEvaluationsActionProcessor = RefreshEvaluationsActionProcessor(
				updateEvaluationsUseCase = UpdateEvaluationsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationsExceptionHandler()
				)
			),
			checkEvaluationFilterActionProcessor = CheckEvaluationFilterActionProcessor(),
			uncheckEvaluationFilterActionProcessor = UncheckEvaluationFilterActionProcessor(),
			clearEvaluationFiltersActionProcessor = ClearEvaluationFiltersActionProcessor(),
			openAddEvaluationActionProcessor = OpenAddEvaluationActionProcessor(),
			pickEvaluationGradeActionProcessor = PickEvaluationGradeActionProcessor(
				getEvaluationUseCase = GetEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			setEvaluationGradeActionProcessor = SetEvaluationGradeActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationExceptionHandler()
				)
			),
			openEvaluationActionProcessor = OpenEvaluationActionProcessor(),
			removeEvaluationActionProcessor = RemoveEvaluationActionProcessor(
				removeEvaluationUseCase = RemoveEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = RemoveEvaluationExceptionHandler()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
