package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.resolveNoAttemptsExplanation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.resolveRecordDataUnavailableExplanation
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
	fun when_stateIsRecordDataUnavailable_then_displaysFailureWithoutRetryAction() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				EvaluationsScreen(
					state = Evaluations.State.RecordDataUnavailable(
						explanation = resolveRecordDataUnavailableExplanation(isNewStudentNoRecord = false)
					),
					onAddEvaluationClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {},
					onRetryClick = {}
				)
			}

			onNodeWithText("Historial no sincronizado").assertExists()
			onNodeWithText(
				"No pudimos leer tu historial académico. " +
					"Cuando se sincronice, tus evaluaciones aparecerán aquí."
			).assertExists()
			// Sync-derived, so the retry the sibling Failed state offers would be inert here.
			assertNodeHidden(BaseUiTags.ErrorViewRetryButton)
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
				state = Evaluations.State.NoAttempts(
					explanation = resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.NoCurrentTerm)
				),
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		onNodeWithText("Sin trimestre en curso").assertExists()
		onNodeWithText("Cuando tengas un trimestre activo, podrás agregar evaluaciones desde aquí.").assertExists()
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_stateIsNoAttemptsBecauseEnrollmentIsUnavailable_then_displaysMessageWithoutActionButton() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				EvaluationsScreen(
					state = Evaluations.State.NoAttempts(
						explanation = resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.EnrollmentUnavailable)
					),
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

	@Test
	fun when_stateIsNoAttemptsBecauseNotEnrolled_then_displaysNotEnrolledCopy() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsScreen(
				state = Evaluations.State.NoAttempts(
					explanation = resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.NotEnrolled)
				),
				onAddEvaluationClick = {},
				onEvaluationClick = { _, _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onRetryClick = {}
			)
		}

		onNodeWithText("No estás inscrito en este trimestre").assertExists()
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_recordDataIsUnavailableForANewStudent_then_displaysNoCurrentTermCopyWithoutRetry() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				EvaluationsScreen(
					state = Evaluations.State.RecordDataUnavailable(
						explanation = resolveRecordDataUnavailableExplanation(isNewStudentNoRecord = true)
					),
					onAddEvaluationClick = {},
					onEvaluationClick = { _, _, _ -> },
					onEvaluationEdit = {},
					onEvaluationDelete = {},
					onRetryClick = {}
				)
			}

			// The record and the summary tell a new student about the missing record; here it only
			// reads as a term that is not there yet, the same as any other empty term.
			onNodeWithText("Sin trimestre en curso").assertExists()
			onNodeWithText("Cuando tengas un trimestre activo, podrás agregar evaluaciones desde aquí.").assertExists()
			onNodeWithText("Aún no tienes expediente").assertDoesNotExist()
			assertNodeHidden(BaseUiTags.ErrorViewRetryButton)
		}

	@Test
	fun when_stateIsContent_then_theListShowsWithNoNoticeAboveIt() = runTuIndiceUiTest {
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
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		assertNodeHidden(BaseUiTags.NoticeView)
	}
}
