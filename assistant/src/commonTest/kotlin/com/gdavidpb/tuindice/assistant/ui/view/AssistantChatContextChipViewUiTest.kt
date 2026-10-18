package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.contextChip
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AssistantChatContextChipViewUiTest {
	@Test
	fun when_theChipIsShown_then_theLabelStartsSixteenDpFromItsStart() = runTuIndiceUiTest {
		val item = contextChip()
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = item, onAction = {})
		}

		val chip = chipBounds()
		val label = onNodeWithText(item.label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertEquals(with(density) { START_MARGIN.toPx() }, label.left - chip.left, absoluteTolerance = PIXEL_TOLERANCE)
	}

	@Test
	fun when_theChipIsShown_then_theLabelIsAsFarFromItsStartAsTheDrawnCrossIsFromItsEnd() = runTuIndiceUiTest {
		val item = contextChip()
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = item, onAction = {})
		}

		val chip = chipBounds()
		val label = onNodeWithText(item.label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val icon = onNodeWithTag(AssistantUiTags.ContextRemove, useUnmergedTree = true).onChild()
			.fetchSemanticsNode().boundsInRoot

		// The cross of the close icon is drawn CROSS_INSET inside its 24 dp box; what reads as the margin is to the cross.
		val crossToEnd = chip.right - icon.right + with(density) { CROSS_INSET.toPx() }

		assertEquals(crossToEnd, label.left - chip.left, absoluteTolerance = with(density) { 1.dp.toPx() })
	}

	private fun ComposeUiTest.chipBounds(): Rect =
		onNodeWithTag(AssistantUiTags.ContextChip).fetchSemanticsNode().boundsInRoot

	private companion object {
		val START_MARGIN = 16.dp
		val CROSS_INSET = 5.dp
		const val PIXEL_TOLERANCE = 1f
	}
}
