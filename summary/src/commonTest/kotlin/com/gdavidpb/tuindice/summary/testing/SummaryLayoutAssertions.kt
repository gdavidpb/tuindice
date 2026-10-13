package com.gdavidpb.tuindice.summary.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The letter scale of a student who set the system's text larger. */
const val LARGE_LETTER_SCALE = 1.3f

/** Lays [content] out with the letter at [LARGE_LETTER_SCALE]; the width comes from the test's own box. */
@Composable
fun WithLargeLetter(content: @Composable () -> Unit) {
	CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = LARGE_LETTER_SCALE)) {
		content()
	}
}

/**
 * The text laid out whole: every character of it is in a line (a text held to one line that does not
 * fit drops the rest), no line ends in an ellipsis and none is wider than the box the layout gave it. With
 * [singleLine] it also has to fit on one line.
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextIsWhole(text: String, singleLine: Boolean = false) {
	val nodes = onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes()

	assertTrue(nodes.isNotEmpty(), "'$text' must be on screen")

	// Every place the text is said (a code is on the chip of a row and on the one of its points).
	nodes.forEach { node ->
		val layouts = mutableListOf<TextLayoutResult>()

		node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)

		val result = layouts.single()
		val boxWidth = node.size.width
		val lines = (0 until result.lineCount)

		assertFalse(result.multiParagraph.didExceedMaxLines, "'$text' must not lose its last lines")
		assertEquals(text.length, result.getLineEnd(result.lineCount - 1), "the lines of '$text' hold all of it")
		assertTrue(lines.none { line -> result.isLineEllipsized(line) }, "'$text' must not be cut with an ellipsis")
		// The width of each line, whatever its alignment puts it at: a centered text is laid out as wide as
		// its parent allows and its lines sit in the middle of that, so their right edge says nothing.
		assertTrue(
			lines.all { line -> result.getLineRight(line) - result.getLineLeft(line) <= boxWidth + 0.5f },
			"every line of '$text' fits the box it was given: " +
				"${lines.map { line -> result.getLineRight(line) - result.getLineLeft(line) }} in $boxWidth"
		)

		if (singleLine) assertEquals(1, result.lineCount, "'$text' must stay on one line")
	}
}
