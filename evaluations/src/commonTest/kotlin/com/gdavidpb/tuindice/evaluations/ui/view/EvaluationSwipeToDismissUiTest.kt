package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
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
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationSwipeActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationSwipeToDismissUiTest {
	private val toggleActionsTag = "toggle_actions"

	@Test
	fun when_rendered_then_displaysContainerAndContentWithoutActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
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
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
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
				actions = EvaluationSwipeActions(
					onAsk = null,
					onEdit = { editClicks++ },
					onDelete = { deleteClicks++ }
				)
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
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
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
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
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

	@Test
	fun when_open_then_theCardSlidesPastItsActionsByAGap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				initiallyOpen = true,
				actions = EvaluationSwipeActions(onAsk = {}, onEdit = {}, onDelete = {})
			) {
				Text(text = "Evaluacion", modifier = Modifier.fillMaxWidth().testTag(cardTag))
			}
		}

		val container = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer).fetchSemanticsNode().boundsInRoot
		val card = onNodeWithTag(cardTag).fetchSemanticsNode().boundsInRoot
		val slide = with(density) { (ActionsWithAskWidth + ActionsGap).toPx() }

		// The card opens past its actions and the gap, so it stops before the first action instead of touching it.
		assertEquals(container.right - slide, card.right, absoluteTolerance = 1f)
	}

	@Test
	fun when_releasedBeforeHalf_then_closesTheRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
			) {
				Text(text = "Evaluacion", modifier = Modifier.fillMaxWidth().testTag(cardTag))
			}
		}

		val slide = with(density) { revealWidth(hasAsk = false).toPx() }

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer).performTouchInput {
			down(centerRight)
			moveBy(Offset(-0.4f * slide, 0f))
			up()
		}
		mainClock.advanceTimeBy(200)
		waitForIdle()

		assertNodeHidden(EvaluationsUiTags.EvaluationSwipeEditAction)
		assertNodeHidden(EvaluationsUiTags.EvaluationSwipeDeleteAction)
		assertEquals(0f, cardOffsetOf(cardTag), absoluteTolerance = 1f)
	}

	@Test
	fun when_releasedPastHalf_then_opensTheRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
			) {
				Text(text = "Evaluacion", modifier = Modifier.fillMaxWidth().testTag(cardTag))
			}
		}

		val slide = with(density) { revealWidth(hasAsk = false).toPx() }

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer).performTouchInput {
			down(centerRight)
			moveBy(Offset(-0.6f * slide, 0f))
			up()
		}
		mainClock.advanceTimeBy(200)
		waitForIdle()

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeEditAction)
		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction)
		assertEquals(-slide, cardOffsetOf(cardTag), absoluteTolerance = 1f)
	}

	@Test
	fun when_draggedWhileAnimating_then_continuesFromTheCurrentOffset() = runTuIndiceUiTest {
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			EvaluationSwipeToDismiss(
				actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
			) { onActionsClick ->
				Text(
					modifier = Modifier
						.testTag(toggleActionsTag)
						.clickable(onClick = onActionsClick),
					text = "Menu"
				)
			}
		}
		waitForIdle()

		val slide = with(density) { revealWidth(hasAsk = false).toPx() }
		val dragPx = 40f

		// The content opens the row, and its 180 ms animation runs for 90 ms before the drag starts.
		onNodeWithTag(toggleActionsTag).performClick()
		mainClock.advanceTimeBy(90)
		waitForIdle()

		val beforeDrag = cardOffsetOf(toggleActionsTag)

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer).performTouchInput {
			down(centerRight)
			moveBy(Offset(-dragPx, 0f))
			up()
		}
		// Read before the clock moves on: the row is where the drag left it, before its settling animation.
		waitForIdle()

		val moved = cardOffsetOf(toggleActionsTag) - beforeDrag

		// Checks that the drag starts mid-animation, moves the row left from there, and that the release settles it
		// open. The test clock seems to move while the gesture is sent, so the exact offset is not known; the 60 px
		// tolerance absorbs that. It does not check the offset at the first move, nor that the animation stops while
		// the finger is down: a row that kept animating under the finger would still pass.
		assertTrue(beforeDrag < -1f && beforeDrag > -slide + 1f, "The row was at $beforeDrag, not mid-animation")
		assertTrue(moved < 0f && moved > -(dragPx + 60f), "The drag moved the row by $moved")

		mainClock.advanceTimeBy(200)
		waitForIdle()

		assertEquals(-slide, cardOffsetOf(toggleActionsTag), absoluteTolerance = 1f)
	}

	@Test
	fun when_dragIsCancelledPastHalf_then_settlesLikeARelease() = runTuIndiceUiTest {
		var pointerEvents = 0

		setTuIndiceTestContent {
			// Consumes from the third event on: the first move starts the row's drag, and the second one cancels it.
			Box(
				modifier = Modifier.pointerInput(Unit) {
					awaitPointerEventScope {
						while (true) {
							val event = awaitPointerEvent(PointerEventPass.Initial)

							if (++pointerEvents > 2) event.changes.forEach { change -> change.consume() }
						}
					}
				}
			) {
				EvaluationSwipeToDismiss(
					actions = EvaluationSwipeActions(onAsk = null, onEdit = {}, onDelete = {})
				) {
					Text(text = "Evaluacion", modifier = Modifier.fillMaxWidth().testTag(cardTag))
				}
			}
		}

		val slide = with(density) { revealWidth(hasAsk = false).toPx() }

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer).performTouchInput {
			down(centerRight)
			moveBy(Offset(-0.7f * slide, 0f))
			moveBy(Offset(-1f, 0f))
			up()
		}
		mainClock.advanceTimeBy(200)
		waitForIdle()

		// Cancelled past half, the row opens as it would on release.
		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeEditAction)
		assertEquals(-slide, cardOffsetOf(cardTag), absoluteTolerance = 1f)
	}

	// The card's distance from the left edge of its row, which a drag or an animation moves.
	private fun ComposeUiTest.cardOffsetOf(tag: String): Float {
		val container = onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer).fetchSemanticsNode()
		val card = onNodeWithTag(tag).fetchSemanticsNode()

		return card.boundsInRoot.left - container.boundsInRoot.left
	}

	private companion object {
		const val cardTag = "evaluation_card"
	}
}
