package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.testing.evaluationsContentState
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationsScreenUiTest {
	@Test
	fun when_stateIsLoading_then_displaysLoadingView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsScreen(
				state = Evaluations.State.Loading,
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsLoadingIndicator)
	}

	@Test
	fun when_stateIsContent_then_displaysContentView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsScreen(
				state = evaluationsContentState(),
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsContentContainer)
	}

	@Test
	fun when_stateIsFailed_then_retryDispatchesCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			EvaluationsScreen(
				state = Evaluations.State.Failed(message = "Comprueba tu conexión"),
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()
		assertEquals(1, retryClicks)
	}

	@Test
	fun when_stateIsEmpty_then_addDispatchesCallback() = runTuIndiceUiTest {
		var addClicks = 0

		setTuIndiceTestContent {
			EvaluationsScreen(
				state = Evaluations.State.Empty,
				onAddEvaluationClick = { addClicks++ },
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewActionButton)
		onNodeWithTag(BaseUiTags.EmptyViewActionButton).performClick()
		assertEquals(1, addClicks)
	}

	@Test
	fun when_stateIsNoAttempts_then_displaysEmptyContainerWithoutActionButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsScreen(
				state = Evaluations.State.NoAttempts(EvaluationsNoAttemptsReason.NoCurrentTerm),
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_stateIsNoAttemptsBecauseEnrollmentIsUnavailable_then_displaysMessageWithoutActionButton() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				EvaluationsScreen(
					state = Evaluations.State.NoAttempts(EvaluationsNoAttemptsReason.EnrollmentUnavailable),
					onAddEvaluationClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {},
					onRetryClick = {}
				)
			}

			onNodeWithText("Servicio de inscripción no disponible").assertExists()
			onNodeWithText(
				"El servicio de inscripción de la universidad no responde. " +
					"Cuando se restablezca, tus evaluaciones aparecerán aquí."
			).assertExists()
			// The outage is not resolved by the user, so the state offers no action.
			assertNodeHidden(BaseUiTags.EmptyViewActionButton)
		}
}
