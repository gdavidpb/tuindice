package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class MetadataLineUiTest {
	@Test
	fun when_rendered_then_theTextStartsWhereTheNameOfATitleStarts() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			MetadataLine(
				modifier = Modifier.testTag(LINE_TAG),
				icon = Icons.Outlined.Schedule,
				text = "7:30–9:30 a. m."
			)
		}

		val line = onNodeWithTag(LINE_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()
		val text = onNodeWithText("7:30–9:30 a. m.", useUnmergedTree = true).getUnclippedBoundsInRoot()
		val start = (text.left - line.left).value

		// The icon takes the 18 dp a title's icon takes, and the text follows it after the 8 dp a title's name follows it.
		assertTrue(abs(start - TEXT_START_DP) <= 0.5f, "the text starts $start dp into the line, not $TEXT_START_DP")
	}

	@Test
	fun when_theTextIsLong_then_itWrapsInsteadOfBeingCut() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(120.dp)) {
				MetadataLine(icon = Icons.Outlined.MeetingRoom, text = LONG_ROOM)
			}
		}

		onNodeWithText(LONG_ROOM).assertIsDisplayed()
		assertWhole(LONG_ROOM)
	}

	// A text laid out in more than one line, the whole of it: no line ends in an ellipsis, and the lines
	// hold every character.
	private fun ComposeUiTest.assertWhole(text: String) {
		val layout = mutableListOf<TextLayoutResult>()

		onNodeWithText(text, useUnmergedTree = true)
			.fetchSemanticsNode()
			.config[SemanticsActions.GetTextLayoutResult]
			.action?.invoke(layout)

		val result = layout.single()

		assertTrue(result.lineCount > 1, "'$text' must take more than one line")
		assertEquals(text.length, result.getLineEnd(result.lineCount - 1), "the lines must hold the whole '$text'")
		assertFalse((0 until result.lineCount).any { line -> result.isLineEllipsized(line) }, "'$text' must not be cut")
	}

	private companion object {
		const val LINE_TAG = "metadata_line"
		const val TEXT_START_DP = 26f
		const val LONG_ROOM = "LAB-FIS-201 Edificio de Laboratorios"
	}
}
