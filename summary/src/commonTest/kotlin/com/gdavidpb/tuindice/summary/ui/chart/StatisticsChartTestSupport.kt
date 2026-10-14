package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText

// A tooltip is found by its title. It appears and leaves through
// animations, so a test waits for it to arrive or to go instead of
// asserting at once.
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.assertTooltipShows(title: String) {
	waitUntil(timeoutMillis = NODE_TIMEOUT_MILLIS) {
		onAllNodesWithText(title, useUnmergedTree = true).fetchSemanticsNodes().size == 1
	}
	onNodeWithText(title, useUnmergedTree = true).assertIsDisplayed()
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.assertNoTooltip(title: String) {
	onAllNodesWithText(title, useUnmergedTree = true).assertCountEquals(0)
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.waitUntilAbsentText(title: String) {
	waitUntil(timeoutMillis = NODE_TIMEOUT_MILLIS) {
		onAllNodesWithText(title, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
	}
}

private const val NODE_TIMEOUT_MILLIS = 5_000L
