package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_APPROVED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PensumGraphCanvasUiTest {
	@Test
	fun when_unselectedNodeIsTapped_then_reportsItAsTheNewSelection() = runTuIndiceUiTest {
		val fixture = GraphCanvasFixture()
		setGraphCanvasContent(fixture = fixture)

		assertNodeHidden(PensumUiTags.focusedNode(SAMPLE_CURRENT_NODE_ID), useUnmergedTree = true)
		onNodeWithTag(PensumUiTags.node(SAMPLE_CURRENT_NODE_ID)).performClick()

		runOnIdle {
			assertEquals(listOf<String?>(SAMPLE_CURRENT_NODE_ID), fixture.selectedNodeChanges)
			assertEquals(emptyList(), fixture.focusedNodeClicks)
		}
	}

	@Test
	fun when_selectedNodeIsTapped_then_reportsAFocusedNodeClickInsteadOfReselecting() =
		runTuIndiceUiTest {
			val fixture = GraphCanvasFixture()
			setGraphCanvasContent(fixture = fixture, selectedNodeId = SAMPLE_CURRENT_NODE_ID)

			onNodeWithTag(PensumUiTags.focusedNode(SAMPLE_CURRENT_NODE_ID), useUnmergedTree = true)
				.assertExists()
			assertNodeHidden(PensumUiTags.focusedNode(SAMPLE_APPROVED_NODE_ID), useUnmergedTree = true)
			onNodeWithTag(PensumUiTags.node(SAMPLE_CURRENT_NODE_ID)).performClick()

			runOnIdle {
				assertEquals(listOf(SAMPLE_CURRENT_NODE_ID), fixture.focusedNodeClicks)
				assertEquals(emptyList(), fixture.selectedNodeChanges)
			}
		}

	@Test
	fun when_statusFilterIsToggled_then_clearsSelectionAndPersistsTheFilterInSession() =
		runTuIndiceUiTest {
			val fixture = GraphCanvasFixture()
			setGraphCanvasContent(fixture = fixture)

			onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.CURRENT))
				.assertIsNotSelected()
				.performClick()
				.assertIsSelected()

			runOnIdle {
				assertEquals(listOf<String?>(null), fixture.selectedNodeChanges)
				assertEquals(
					setOf(PensumNodeStatusType.CURRENT.name),
					fixture.sessionState.activeStatusFilterNames
				)
			}
		}

	@Test
	fun when_sessionCarriesActiveFilters_then_legendRestoresThemAndClearEmptiesTheSession() =
		runTuIndiceUiTest {
			val fixture = GraphCanvasFixture()
			fixture.sessionState.activeStatusFilterNames = setOf(PensumNodeStatusType.BLOCKED.name)
			setGraphCanvasContent(fixture = fixture)

			onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.BLOCKED)).assertIsSelected()
			onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.APPROVED)).assertIsNotSelected()
			onNodeWithTag(PensumUiTags.StatusFilterClear).performClick()
			waitForIdle()

			onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.BLOCKED)).assertIsNotSelected()
			assertNodeHidden(PensumUiTags.StatusFilterClear)
			assertEquals(emptySet(), fixture.sessionState.activeStatusFilterNames)
		}

	@Test
	fun when_subjectSheetIsVisible_then_legendAndZoomControlsAreNotOffered() = runTuIndiceUiTest {
		setGraphCanvasContent(fixture = GraphCanvasFixture(), isSubjectSheetVisible = true)

		assertNodeVisible(PensumUiTags.Canvas)
		assertNodeVisible(PensumUiTags.node(SAMPLE_CURRENT_NODE_ID))
		assertNodeHidden(PensumUiTags.CanvasLegend)
		assertNodeHidden(PensumUiTags.ZoomIn)
		assertNodeHidden(PensumUiTags.ZoomOut)
	}

	@Test
	fun when_zoomInIsPressed_then_sessionRemembersALargerScaleThanTheInitialOne() = runTuIndiceUiTest {
		val fixture = GraphCanvasFixture()
		setGraphCanvasContent(fixture = fixture)
		waitForIdle()

		assertEquals(InitialCanvasZoom, fixture.sessionState.canvasScale)
		onNodeWithTag(PensumUiTags.ZoomIn).performClick()
		advanceAnimationsBy((ZoomAnimationMillis * 2).toLong())

		assertTrue(
			actual = checkNotNull(fixture.sessionState.canvasScale) > InitialCanvasZoom,
			message = "Expected zoom in to store a scale above $InitialCanvasZoom."
		)
		assertTrue(fixture.sessionState.isMinimapToggleVisible)
	}

	@Test
	fun when_canvasIsDragged_then_itMovesWithoutRecomposingTheNodeCardsOrTheStickyHeader() = runTuIndiceUiTest {
		val fixture = GraphCanvasFixture()
		setGraphCanvasContent(fixture = fixture)
		waitForIdle()
		val offsetBefore = fixture.sessionState.canvasOffsetX to fixture.sessionState.canvasOffsetY
		assertTrue(
			fixture.compositions.any { name -> name.startsWith("pensum-node:") },
			"The probe must see the first composition."
		)
		assertTrue("pensum-header" in fixture.compositions, "The sticky header must show at the initial zoom.")
		fixture.compositions.clear()

		onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
			swipe(start = center, end = center + Offset(x = -60f, y = -40f), durationMillis = 400)
		}
		advanceAnimationsBy((CanvasManualGestureIdleMillis + CanvasSnapDelayMillis + ZoomAnimationMillis * 2).toLong())
		waitForIdle()

		assertNotEquals(offsetBefore, fixture.sessionState.canvasOffsetX to fixture.sessionState.canvasOffsetY)
		assertMovedWithoutRecomposingPerFrame(fixture.compositions)
	}

	@Test
	fun when_canvasIsPinched_then_itZoomsWithoutRecomposingTheNodeCardsOrTheStickyHeader() = runTuIndiceUiTest {
		val fixture = GraphCanvasFixture()
		setGraphCanvasContent(fixture = fixture)
		waitForIdle()
		fixture.compositions.clear()

		onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
			pinch(
				start0 = center + Offset(x = -40f, y = 0f),
				end0 = center + Offset(x = -90f, y = 0f),
				start1 = center + Offset(x = 40f, y = 0f),
				end1 = center + Offset(x = 90f, y = 0f),
				durationMillis = 400
			)
		}
		advanceAnimationsBy((CanvasManualGestureIdleMillis + CanvasSnapDelayMillis + ZoomAnimationMillis * 2).toLong())
		waitForIdle()

		assertTrue(
			actual = checkNotNull(fixture.sessionState.canvasScale) > InitialCanvasZoom,
			message = "Expected the pinch to store a scale above $InitialCanvasZoom."
		)
		assertMovedWithoutRecomposingPerFrame(fixture.compositions)
	}

	@Test
	fun when_fingerMovesOnTheCanvas_then_overlaysStayHiddenWhileEachSignalRestartsTheIdleWindow() =
		runTuIndiceUiTest {
			setGraphCanvasContent(fixture = GraphCanvasFixture())
			waitForIdle()
			mainClock.autoAdvance = false

			onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
				down(center)
				moveBy(Offset(x = 60f, y = 0f))
			}
			advanceAnimationsBy((CanvasOverlayAnimationMillis + 100).toLong())
			assertNodeHidden(PensumUiTags.ZoomIn)
			assertNodeHidden(PensumUiTags.CanvasLegend)

			// This signal, at about 300 ms, restarts the window: the first one would have ended at about 330 ms,
			// so at about 500 ms the controls are still hidden.
			onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
				moveBy(Offset(x = 20f, y = 0f))
			}
			advanceAnimationsBy(200L)
			assertNodeHidden(PensumUiTags.ZoomIn)
			assertNodeHidden(PensumUiTags.CanvasLegend)

			onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
				up()
			}
		}

	@Test
	fun when_fingerRestsAfterMovingForTheIdleWindow_then_controlsReturnWhileTheFingerIsStillDown() =
		runTuIndiceUiTest {
			setGraphCanvasContent(fixture = GraphCanvasFixture())
			waitForIdle()
			mainClock.autoAdvance = false

			onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
				down(center)
				moveBy(Offset(x = 60f, y = 0f))
			}
			// No signal follows the move, so the idle window ends while the finger is still down.
			advanceAnimationsBy((CanvasManualGestureIdleMillis + CanvasOverlayAnimationMillis + 100).toLong())
			assertNodeVisible(PensumUiTags.ZoomIn)
			assertNodeVisible(PensumUiTags.CanvasLegend)

			onNodeWithTag(PensumUiTags.CanvasGestureLayer).performTouchInput {
				up()
			}
		}
}

// The canvas ("pensum-canvas") recomposes at most at the start and at the end of a gesture (to hide and show its
// overlays), never on its frames, and the node cards ("pensum-node") and the sticky header ("pensum-header") never do.
private fun assertMovedWithoutRecomposingPerFrame(compositions: List<String>) {
	assertEquals(emptyList(), compositions.filterNot { name -> name == "pensum-canvas" })
	assertTrue(
		actual = compositions.count { name -> name == "pensum-canvas" } <= 2,
		message = "The canvas recomposed on the gesture's frames: $compositions"
	)
}

private class GraphCanvasFixture {
	val compositions = mutableListOf<String>()
	val selectedNodeChanges = mutableListOf<String?>()
	val focusedNodeClicks = mutableListOf<String>()
	val sessionState = PensumScreenSessionStore.SelectionState()
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setGraphCanvasContent(
	fixture: GraphCanvasFixture,
	selectedNodeId: String? = null,
	isSubjectSheetVisible: Boolean = false
) {
	setTuIndiceTestContent {
		CompositionLocalProvider(
			LocalTuIndiceCompositionProbe provides { name -> fixture.compositions += name },
			LocalPensumCanvasClockMillis provides { mainClock.currentTime }
		) {
			PensumGraphCanvas(
				model = samplePensumScreenModel(),
				selectedNodeId = selectedNodeId,
				onSelectedNodeChange = { nodeId -> fixture.selectedNodeChanges += nodeId },
				onFocusedNodeClick = { nodeId -> fixture.focusedNodeClicks += nodeId },
				isSubjectSheetVisible = isSubjectSheetVisible,
				sessionState = fixture.sessionState
			)
		}
	}
}
