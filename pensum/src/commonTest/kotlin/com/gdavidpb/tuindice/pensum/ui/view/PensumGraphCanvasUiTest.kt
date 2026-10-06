package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
}

private class GraphCanvasFixture {
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
