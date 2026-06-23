package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.intl.Locale
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.EnsureEvaluationsLoadedUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * RTL and accessibility-semantics matrix for the Evaluations list screen.
 *
 * Mirrors the fixture construction of [EvaluationsRouteUiTest] and reuses the
 * evaluations testing doubles without modifying them.
 */
@OptIn(ExperimentalTestApi::class)
class EvaluationsRtlA11yUiTest {
	@Test
	fun when_layoutIsRtl_then_listFabAndItemsRemainVisible() = runTuIndiceUiTest {
		val viewModel = createViewModel()

		setTuIndiceTestContent(locale = Locale("ar")) {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.evaluationItemCard(DEFAULT_PENDING_EVALUATION.id))
				.fetchSemanticsNodes().isNotEmpty()
		}

		// E2E-critical nodes: list, primary FAB and both evaluation cards.
		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		assertNodeVisible(EvaluationsUiTags.EvaluationsAddFab)
		assertNodeEnabled(EvaluationsUiTags.EvaluationsAddFab)
		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(DEFAULT_PENDING_EVALUATION.id)).assertExists()
		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(DEFAULT_COMPLETED_EVALUATION.id)).assertExists()
	}

	@Test
	fun when_layoutIsRtl_then_addFabStillNavigatesToAddScreen() = runTuIndiceUiTest {
		val viewModel = createViewModel()
		var addNavigationCalls = 0

		setTuIndiceTestContent(locale = Locale("ar")) {
			EvaluationsRoute(
				onNavigateToAddEvaluation = { addNavigationCalls++ },
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationsAddFab)
				.fetchSemanticsNodes().isNotEmpty()
		}

		// Mirrored layout keeps the FAB tappable in its flipped corner.
		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).performClick()

		waitUntil(timeoutMillis = 2_000) {
			addNavigationCalls > 0
		}

		assertEquals(1, addNavigationCalls)
	}

	@Test
	fun when_a11ySemanticsInspected_then_fabAndGradeActionExposeLabels() = runTuIndiceUiTest {
		val viewModel = createViewModel()

		setTuIndiceTestContent {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.evaluationItemCard(DEFAULT_COMPLETED_EVALUATION.id))
				.fetchSemanticsNodes().isNotEmpty()
		}

		// FAB: icon-only control labeled via contentDescription (a11y_add_evaluation).
		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab)
			.assert(hasClickAction())
			.assert(hasContentDescription("Agregar evaluación"))

		// Grade action on the completed evaluation: clickable and labeled by its grade text.
		onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(DEFAULT_COMPLETED_EVALUATION.id))
			.assert(hasClickAction())
			.assert(hasAccessibleLabel)
	}

	@Test
	fun when_compactRtlWithIncreasedDensity_then_listRemainsComposedAndFabEnabled() = runTuIndiceUiTest {
		val viewModel = createViewModel()

		// Compact size class plus 1.25x density (the kit's scale knob).
		setTuIndiceTestContent(
			sizeClass = TuIndiceTestSizeClass.Compact,
			density = 1.25f,
			locale = Locale("ar")
		) {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {},
				onNavigateToEvaluation = {},
				onNavigateToEvaluationGradePickerDialog = { _, _, _, _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.evaluationItemCard(DEFAULT_PENDING_EVALUATION.id))
				.fetchSemanticsNodes().isNotEmpty()
		}

		// The list keeps composing items at the larger scale; the bottom-anchored FAB
		// may sit at the scaled viewport edge, so assert existence + enabled state.
		assertNodeVisible(EvaluationsUiTags.EvaluationsList)
		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(DEFAULT_PENDING_EVALUATION.id)).assertExists()
		onNodeWithTag(EvaluationsUiTags.EvaluationsAddFab).assertExists().assertIsEnabled()
	}

	private fun createViewModel(): EvaluationsViewModel {
		val repository = RecordingEvaluationRepository(
			evaluationsFlow = flowOf(
				listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)

		return EvaluationsViewModel(
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					syncStatusRepository = RecordingSyncStatusRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				ensureEvaluationsLoadedUseCase = EnsureEvaluationsLoadedUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				updateEvaluationsUseCase = UpdateEvaluationsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				getEvaluationUseCase = GetEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationExceptionHandler()
				),
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

/**
 * A node is considered accessible when it exposes a contentDescription,
 * regular text or editable text to assistive technologies.
 */
private val hasAccessibleLabel: SemanticsMatcher =
	SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)
		.or(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
		.or(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))
