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
	fun when_theChipIsShown_then_theLabelStartsTwelveDpFromItsStart() = runTuIndiceUiTest {
		val item = contextChip()
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = item, onAction = {})
		}

		val chip = chipBounds()
		val label = onNodeWithText(item.label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertEquals(with(density) { START_MARGIN.toPx() }, label.left - chip.left, absoluteTolerance = PIXEL_TOLERANCE)
	}

	@Test
	fun when_theChipIsShown_then_theLabelIsAsFarFromItsStartAsTheCloseIconIsFromItsEnd() = runTuIndiceUiTest {
		val item = contextChip()
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = item, onAction = {})
		}

		val chip = chipBounds()
		val label = onNodeWithText(item.label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val icon = onNodeWithTag(AssistantUiTags.ContextRemove, useUnmergedTree = true).onChild()
			.fetchSemanticsNode().boundsInRoot

		assertEquals(chip.right - icon.right, label.left - chip.left, absoluteTolerance = PIXEL_TOLERANCE)
	}

	private fun ComposeUiTest.chipBounds(): Rect =
		onNodeWithTag(AssistantUiTags.ContextChip).fetchSemanticsNode().boundsInRoot

	private companion object {
		val START_MARGIN = 12.dp
		const val PIXEL_TOLERANCE = 1f
	}
}
