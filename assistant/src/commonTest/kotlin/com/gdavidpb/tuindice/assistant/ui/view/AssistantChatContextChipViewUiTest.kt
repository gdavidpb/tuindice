package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.contextChip
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatContextChipViewUiTest {
	@Test
	fun when_theChipIsShown_then_theCaptionSobreEstaPantallaSitsAboveIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = contextChip(), onAction = {})
		}

		val caption = onNodeWithText(CAPTION_TEXT).fetchSemanticsNode().boundsInRoot

		assertTrue(caption.bottom <= chipBounds().top)
	}

	@Test
	fun when_theChipIsShown_then_itTakesTheFullWidth_asTheSuggestionsDo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = contextChip(), onAction = {}, modifier = Modifier.width(CONTAINER_WIDTH))
		}

		assertEquals(with(density) { CONTAINER_WIDTH.toPx() }, chipBounds().width, absoluteTolerance = PIXEL_TOLERANCE)
	}

	@Test
	fun when_theChipIsShown_then_theLabelFollowsTheScreenIcon_asTheTextOfASuggestion() = runTuIndiceUiTest {
		val item = contextChip()
		setTuIndiceTestContent {
			AssistantChatContextChipView(item = item, onAction = {})
		}

		val chip = chipBounds()
		val label = onNodeWithText(item.label, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		// 12 dp to the screen's icon, the 20 dp icon and 12 dp to the label.
		val labelStart = START_MARGIN + ICON_SIZE + ICON_GAP

		assertEquals(with(density) { labelStart.toPx() }, label.left - chip.left, absoluteTolerance = PIXEL_TOLERANCE)
	}

	private fun ComposeUiTest.chipBounds(): Rect =
		onNodeWithTag(AssistantUiTags.ContextChip).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val CAPTION_TEXT = "Sobre esta pantalla"
		val START_MARGIN = 12.dp
		val ICON_SIZE = 20.dp
		val ICON_GAP = 12.dp
		val CONTAINER_WIDTH = 320.dp
		const val PIXEL_TOLERANCE = 1f
	}
}
