package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceModalBottomSheetUiTest {
	// The test reads the state of the sheet it composes, through this holder.
	private class StateHolder {
		var state: TuIndiceSheetState? by mutableStateOf(null)
	}

	@Test
	fun when_sheetIsShown_then_itShowsItsContent() = runTuIndiceUiTest {
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				onDismissRequest = {},
				sheetState = sheetState
			) {
				Text(text = CONTENT_TEXT)
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_theScrimAboveTheSheetIsTapped_then_theSheetIsDismissedOnce() = runTuIndiceUiTest {
		var dismissCalls = 0
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				onDismissRequest = { dismissCalls++ },
				sheetState = sheetState
			) {
				Box(modifier = Modifier.fillMaxWidth().height(SHORT_CONTENT_DP.dp))
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		// The sheet sits at the foot of the window; a touch at its top edge lands on the scrim, which is drawn behind it.
		onNodeWithTag(BaseUiTags.ModalBottomSheetScrim).assertIsDisplayed()
		onAllNodes(isRoot())[0].performTouchInput { click(Offset(width / 2f, SCRIM_TAP_Y)) }

		waitUntil(timeoutMillis = WAIT_MILLIS) { dismissCalls > 0 }
		waitForIdle()
		assertEquals(1, dismissCalls)
		assertEquals(TuIndiceSheetValue.Hidden, holder.state?.currentValue)
	}

	@Test
	fun when_sheetIsDraggedDown_then_theSheetIsDismissed() = runTuIndiceUiTest {
		var dismissCalls = 0
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				onDismissRequest = { dismissCalls++ },
				sheetState = sheetState
			) {
				Box(modifier = Modifier.fillMaxWidth().height(SHORT_CONTENT_DP.dp).testTag(CONTENT_TAG))
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		// The swipe starts on the content, which is drawn where the sheet is. The sheet's own node is laid out at the top
		// of the window and drawn at its offset, so a swipe on its bounds started on the scrim above the sheet and
		// closed it with a tap, not with the drag.
		onNodeWithTag(CONTENT_TAG).performTouchInput { swipeDown() }

		waitUntil(timeoutMillis = WAIT_MILLIS) { dismissCalls > 0 }
		waitForIdle()
		assertEquals(1, dismissCalls)
		assertEquals(TuIndiceSheetValue.Hidden, holder.state?.currentValue)
	}

	@Test
	fun when_sheetIsDraggedDownPastHalfwayAndSlowsBeforeRelease_then_theSheetIsDismissed() = runTuIndiceUiTest {
		var dismissCalls = 0
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				onDismissRequest = { dismissCalls++ },
				sheetState = sheetState
			) {
				Box(modifier = Modifier.fillMaxWidth().height(GROWN_CONTENT_DP.dp).testTag(CONTENT_TAG))
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		dragDownSlowingBeforeRelease(CONTENT_TAG)

		waitUntil(timeoutMillis = WAIT_MILLIS) { dismissCalls > 0 }
		waitForIdle()
		assertEquals(1, dismissCalls)
		assertEquals(TuIndiceSheetValue.Hidden, holder.state?.settledValue)
	}

	@Test
	fun when_theScrollingContentAtItsTopIsDraggedDownPastHalfwayAndSlowsBeforeRelease_then_theSheetIsDismissed() =
		runTuIndiceUiTest {
			var dismissCalls = 0
			val holder = StateHolder()

			setTuIndiceTestContent {
				val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
				SideEffect { holder.state = sheetState }

				TuIndiceModalBottomSheet(
					onDismissRequest = { dismissCalls++ },
					sheetState = sheetState
				) {
					// Content that scrolls, at its top: a drag down cannot scroll it, so what it leaves moves the sheet.
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.height(GROWN_CONTENT_DP.dp)
							.verticalScroll(rememberScrollState())
							.testTag(CONTENT_TAG)
					) {
						Box(modifier = Modifier.fillMaxWidth().height(SCROLLING_CONTENT_DP.dp))
					}
				}
			}

			waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
			dragDownSlowingBeforeRelease(CONTENT_TAG)

			waitUntil(timeoutMillis = WAIT_MILLIS) { dismissCalls > 0 }
			waitForIdle()
			assertEquals(1, dismissCalls)
			assertEquals(TuIndiceSheetValue.Hidden, holder.state?.settledValue)
		}

	// Drags the node with this tag down, as a finger does and as XCUITest's swipe does: it speeds up, slows down before
	// it lets go, and lets go where it stopped. The sheet moves with the finger, so the finger slows down relative to the
	// sheet's own frame; the speed that settles the sheet is the finger's on the screen, which never goes up. Each step
	// comes in a frame of its own, as on a device, so the sheet is laid out where the step left it. The steps are given
	// on the window, which does not move: the node moves with the sheet, under the finger.
	private fun ComposeUiTest.dragDownSlowingBeforeRelease(tag: String) {
		val bounds = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
		val window = onAllNodes(isRoot())[0]

		window.performTouchInput { down(Offset(bounds.center.x, bounds.top + DRAG_START_Y)) }
		DECELERATING_STEPS.forEach { step ->
			window.performTouchInput { moveBy(Offset(0f, step.dp.toPx()), delayMillis = STEP_MILLIS) }
		}
		window.performTouchInput { up() }
	}

	@Test
	fun when_theContentOfAnOpenSheetGrowsMoreThanTwice_then_theSheetIsNotDismissed() = runTuIndiceUiTest {
		var dismissCalls = 0
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
			var grown by remember { mutableStateOf(false) }
			SideEffect { holder.state = sheetState }

			// As the share sheet's card does when its art arrives: the sheet has opened, and its content then takes more
			// than twice its height, so where it rests is closer to the hidden anchor than to the new open one.
			LaunchedEffect(sheetState) {
				snapshotFlow { sheetState.settledValue }.first { value -> value == TuIndiceSheetValue.Expanded }
				grown = true
			}

			TuIndiceModalBottomSheet(
				onDismissRequest = { dismissCalls++ },
				sheetState = sheetState
			) {
				Box(modifier = Modifier.fillMaxWidth().height(if (grown) GROWN_CONTENT_DP.dp else SHORT_CONTENT_DP.dp))
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.settledValue == TuIndiceSheetValue.Expanded }
		waitForIdle()

		assertEquals(0, dismissCalls)
		assertEquals(TuIndiceSheetValue.Expanded, holder.state?.settledValue)
	}

	@Test
	fun when_sheetIsPartiallyExpandedAndSwipedUp_then_itBecomesExpanded() = runTuIndiceUiTest {
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState()
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				modifier = Modifier.testTag(SURFACE_TAG),
				onDismissRequest = {},
				sheetState = sheetState
			) {
				Box(modifier = Modifier.fillMaxWidth().height(TALL_CONTENT_DP.dp))
			}
		}

		// A sheet taller than half the window opens half open, and the first swipe up takes it to its full height.
		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.PartiallyExpanded }
		// The half-open sheet's lower part is below the window, so the swipe starts on the part that shows.
		onNodeWithTag(SURFACE_TAG).performTouchInput {
			swipeUp(startY = top + visibleSize.height * VISIBLE_SWIPE_START, endY = top)
		}
		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
	}

	@Test
	fun when_theCallerHidesTheSheet_then_theDismissalIsNotReported() = runTuIndiceUiTest {
		var dismissCalls = 0
		val holder = StateHolder()
		var hideRequested by mutableStateOf(false)

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
			SideEffect { holder.state = sheetState }

			// The caller hides the sheet from its own coroutine, as a screen does when it closes the sheet.
			LaunchedEffect(hideRequested) {
				if (hideRequested) sheetState.hide()
			}

			TuIndiceModalBottomSheet(
				onDismissRequest = { dismissCalls++ },
				sheetState = sheetState
			) {
				Text(text = CONTENT_TEXT)
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		hideRequested = true
		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Hidden }
		waitForIdle()

		assertEquals(0, dismissCalls)
	}

	@Test
	fun when_animationsAreDisabled_then_theSheetOpensAtOnce() = runTuIndiceUiTest {
		val holder = StateHolder()
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)
				SideEffect { holder.state = sheetState }

				TuIndiceModalBottomSheet(
					onDismissRequest = {},
					sheetState = sheetState
				) {
					Text(text = CONTENT_TEXT)
				}
			}
		}

		// Only frames that lay the sheet out pass: with animations on, the sheet would still be sliding in from the foot of
		// the window after them.
		repeat(OPENING_FRAMES) { mainClock.advanceTimeByFrame() }
		assertEquals(TuIndiceSheetValue.Expanded, holder.state?.currentValue)
		onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_sheetCannotBeHidden_then_aTapOnTheScrimKeepsItOpen() = runTuIndiceUiTest {
		var dismissCalls = 0
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true, hideable = false)
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				onDismissRequest = { dismissCalls++ },
				sheetState = sheetState
			) {
				Text(text = CONTENT_TEXT)
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		onAllNodes(isRoot())[0].performTouchInput { click(Offset(width / 2f, SCRIM_TAP_Y)) }
		waitForIdle()

		assertEquals(0, dismissCalls)
		assertEquals(TuIndiceSheetValue.Expanded, holder.state?.currentValue)
	}

	@Test
	fun when_sheetIsShown_then_itExposesItsPaneTitleAndItsDismissAndExpandActions() = runTuIndiceUiTest {
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState()
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				modifier = Modifier.testTag(SURFACE_TAG),
				onDismissRequest = {},
				sheetState = sheetState
			) {
				Box(modifier = Modifier.fillMaxWidth().height(TALL_CONTENT_DP.dp))
			}
		}

		// A sheet taller than half the window opens half open: it can be dismissed and expanded from there.
		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.PartiallyExpanded }
		onNodeWithTag(SURFACE_TAG).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
		onNodeWithTag(SURFACE_TAG).assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
		onNodeWithTag(SURFACE_TAG).assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Expand))
	}

	@Test
	fun when_sheetCannotBeHidden_then_itExposesNoDismissAction() = runTuIndiceUiTest {
		val holder = StateHolder()

		setTuIndiceTestContent {
			val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true, hideable = false)
			SideEffect { holder.state = sheetState }

			TuIndiceModalBottomSheet(
				modifier = Modifier.testTag(SURFACE_TAG),
				onDismissRequest = {},
				sheetState = sheetState
			) {
				Text(text = CONTENT_TEXT)
			}
		}

		waitUntil(timeoutMillis = WAIT_MILLIS) { holder.state?.currentValue == TuIndiceSheetValue.Expanded }
		onNodeWithTag(SURFACE_TAG).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.Dismiss))
	}

	private companion object {
		const val CONTENT_TEXT = "Contenido de la hoja"
		const val SURFACE_TAG = "tuindice_modal_bottom_sheet_test_surface"
		const val CONTENT_TAG = "tuindice_modal_bottom_sheet_test_content"
		const val OPENING_FRAMES = 10
		const val SHORT_CONTENT_DP = 200
		const val TALL_CONTENT_DP = 400
		const val GROWN_CONTENT_DP = 600
		const val SCROLLING_CONTENT_DP = 1_200
		const val SCRIM_TAP_Y = 20f
		const val VISIBLE_SWIPE_START = 0.8f
		const val WAIT_MILLIS = 5_000L
		const val DRAG_START_Y = 10f
		const val STEP_MILLIS = 16L
		const val CRUISE_STEPS = 20

		// A swipe down of two thirds of the sheet, one step a frame: up to speed, then slower to the release, where it
		// lets go without moving. It ends well past the middle between the open and the hidden anchors.
		val DECELERATING_STEPS = listOf(10, 20, 30) + List(CRUISE_STEPS) { 40 } + listOf(30, 20, 10, 5)
	}
}
