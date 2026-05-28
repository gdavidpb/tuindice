package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EvaluationSwipeToDismissUiTest {
	private val toggleActionsTag = "toggle_actions"

	@Test
	fun when_rendered_then_displaysContainerAndContentWithoutActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				onEdit = {},
				onDelete = {}
			) {
				Text(text = "Evaluacion")
			}
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
		onNodeWithText("Evaluacion").assertIsDisplayed()
		assertNodeHidden(EvaluationsUiTags.EvaluationSwipeEditAction)
		assertNodeHidden(EvaluationsUiTags.EvaluationSwipeDeleteAction)
	}

	@Test
	fun when_swipedLeft_then_revealsEditAndDeleteActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				onEdit = {},
				onDelete = {}
			) {
				Text(text = "Evaluacion")
			}
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty() &&
				onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).fetchSemanticsNodes().isNotEmpty()
		}
	}

	@Test
	fun when_actionsTapped_then_invokesCallbacks() = runTuIndiceUiTest {
		var editClicks = 0
		var deleteClicks = 0

		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				onEdit = { editClicks++ },
				onDelete = { deleteClicks++ }
			) {
				Text(text = "Evaluacion")
			}
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).performClick()

		assertEquals(1, editClicks)
		assertEquals(0, deleteClicks)

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeDeleteAction).performClick()

		assertEquals(1, editClicks)
		assertEquals(1, deleteClicks)
	}

	@Test
	fun when_contentCallbackTapped_then_togglesActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				onEdit = {},
				onDelete = {}
			) { onActionsClick ->
				Text(
					modifier = Modifier
						.testTag(toggleActionsTag)
						.clickable(onClick = onActionsClick),
					text = "Menu"
				)
			}
		}

		onNodeWithTag(toggleActionsTag).performClick()

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(toggleActionsTag).performClick()

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isEmpty()
		}
	}

	@Test
	fun when_swipedBack_then_closesActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				onEdit = {},
				onDelete = {}
			) {
				Text(text = "Evaluacion")
			}
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeRight() }

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(EvaluationsUiTags.EvaluationSwipeEditAction).fetchSemanticsNodes().isEmpty()
		}
	}
}
