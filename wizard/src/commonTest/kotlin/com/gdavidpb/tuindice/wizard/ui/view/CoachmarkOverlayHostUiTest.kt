package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import com.gdavidpb.tuindice.wizard.ui.anchor.CoachmarkAnchorRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CoachmarkOverlayHostUiTest {
	@Test
	fun when_stateHasNoActiveCoachmark_then_bubbleIsNotRendered() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CoachmarkOverlayHost(
				state = CoachmarkOverlay.State(),
				anchorRegistry = CoachmarkAnchorRegistry(),
				onPreviousActionClick = {},
				onPrimaryActionClick = {}
			)
		}

		onAllNodesWithTag(CoachmarkUiTags.Bubble).assertCountEquals(0)
	}

	@Test
	fun when_anchorIsMissing_then_bubbleIsNotRendered() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CoachmarkOverlayHost(
				state = summaryState(),
				anchorRegistry = CoachmarkAnchorRegistry(),
				onPreviousActionClick = {},
				onPrimaryActionClick = {}
			)
		}

		onAllNodesWithTag(CoachmarkUiTags.Bubble).assertCountEquals(0)
	}

	@Test
	fun when_anchorExists_then_bubbleIsRenderedWithConfirmAction() = runTuIndiceUiTest {
		val registry = CoachmarkAnchorRegistry().apply {
			update(
				id = CoachmarkId.Summary,
				bounds = Rect(
					offset = Offset(x = 40f, y = 96f),
					size = Size(width = 260f, height = 120f)
				)
			)
		}
		var primaryClickCount = 0

		setTuIndiceTestContent {
			CoachmarkOverlayHost(
				state = summaryState(),
				anchorRegistry = registry,
				onPreviousActionClick = {},
				onPrimaryActionClick = { primaryClickCount++ }
			)
		}

		assertNodeVisible(CoachmarkUiTags.Host)
		assertNodeVisible(CoachmarkUiTags.Bubble)
		onAllNodesWithTag(CoachmarkUiTags.BackButton).assertCountEquals(0)
		onNodeWithTag(CoachmarkUiTags.ConfirmButton).assertTextEquals("Entendido")

		onNodeWithTag(CoachmarkUiTags.ConfirmButton).performClick()

		assertEquals(1, primaryClickCount)
	}

	@Test
	fun when_stateHasNextCoachmark_then_primaryActionTextIsNext() = runTuIndiceUiTest {
		val registry = CoachmarkAnchorRegistry().apply {
			update(
				id = CoachmarkId.Record,
				bounds = Rect(
					offset = Offset(x = 40f, y = 96f),
					size = Size(width = 260f, height = 120f)
				)
			)
		}

		setTuIndiceTestContent {
			CoachmarkOverlayHost(
				state = recordSequenceState(),
				anchorRegistry = registry,
				onPreviousActionClick = {},
				onPrimaryActionClick = {}
			)
		}

		onAllNodesWithTag(CoachmarkUiTags.BackButton).assertCountEquals(0)
		onNodeWithTag(CoachmarkUiTags.ConfirmButton).assertTextEquals("Siguiente")
	}

	@Test
	fun when_stateHasPreviousCoachmark_then_backActionIsRenderedAndClickable() = runTuIndiceUiTest {
		val registry = CoachmarkAnchorRegistry().apply {
			update(
				id = CoachmarkId.RecordControls,
				bounds = Rect(
					offset = Offset(x = 40f, y = 96f),
					size = Size(width = 260f, height = 120f)
				)
			)
		}
		var previousClickCount = 0

		setTuIndiceTestContent {
			CoachmarkOverlayHost(
				state = recordControlsState(),
				anchorRegistry = registry,
				onPreviousActionClick = { previousClickCount++ },
				onPrimaryActionClick = {}
			)
		}

		onNodeWithTag(CoachmarkUiTags.BackButton).assertTextEquals("Atrás")
		onNodeWithTag(CoachmarkUiTags.ConfirmButton).assertTextEquals("Entendido")

		onNodeWithTag(CoachmarkUiTags.BackButton).performClick()

		assertEquals(1, previousClickCount)
	}

	@Test
	fun when_anchorIsNearTop_then_bubbleIsRenderedNearBottom() = runTuIndiceUiTest {
		val registry = CoachmarkAnchorRegistry().apply {
			update(
				id = CoachmarkId.Summary,
				bounds = Rect(
					offset = Offset(x = 40f, y = 96f),
					size = Size(width = 260f, height = 120f)
				)
			)
		}

		setTuIndiceTestContent {
			CoachmarkOverlayHost(
				state = summaryState(),
				anchorRegistry = registry,
				onPreviousActionClick = {},
				onPrimaryActionClick = {}
			)
		}

		val hostBounds = onNodeWithTag(CoachmarkUiTags.Host).getUnclippedBoundsInRoot()
		val bubbleBounds = onNodeWithTag(CoachmarkUiTags.Bubble).getUnclippedBoundsInRoot()
		val hostHeight = hostBounds.bottom - hostBounds.top

		assertTrue(bubbleBounds.top > hostBounds.top + hostHeight / 2)
	}

	private fun summaryState(): CoachmarkOverlay.State {
		val coachmark = contextualCoachmarks().first { coachmark ->
			coachmark.id == CoachmarkId.Summary
		}

		return CoachmarkOverlay.State(
			activeCoachmark = coachmark,
			pendingCoachmarks = listOf(coachmark)
		)
	}

	private fun recordSequenceState(): CoachmarkOverlay.State {
		val coachmarks = contextualCoachmarks().filter { coachmark ->
			coachmark.id == CoachmarkId.Record || coachmark.id == CoachmarkId.RecordControls
		}

		return CoachmarkOverlay.State(
			activeCoachmark = coachmarks.first(),
			pendingCoachmarks = coachmarks
		)
	}

	private fun recordControlsState(): CoachmarkOverlay.State {
		val coachmarks = contextualCoachmarks().filter { coachmark ->
			coachmark.id == CoachmarkId.Record || coachmark.id == CoachmarkId.RecordControls
		}

		return CoachmarkOverlay.State(
			activeCoachmark = coachmarks.last(),
			previousCoachmarks = listOf(coachmarks.first()),
			pendingCoachmarks = listOf(coachmarks.last())
		)
	}
}
