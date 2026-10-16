package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RichTooltipLinesUiTest {
	@Test
	fun when_theBodyIsOneLine_then_theTagMarksTheLineItself() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RichTooltipLines(tooltip = RichTooltipText(lines = listOf("Índice 4.2"), testTag = BodyTag))
		}

		assertNodeVisible(BodyTag)
		onNodeWithTag(BodyTag).assertTextEquals("Índice 4.2")
	}

	@Test
	fun when_theBodyHasSeveralLines_then_theTagMarksTheStackAndEveryLineIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RichTooltipLines(tooltip = RichTooltipText(lines = listOf("Primera", "Segunda"), testTag = BodyTag))
		}

		onNodeWithTag(BodyTag).onChildren().assertCountEquals(2)
		onNodeWithText("Primera").assertIsDisplayed()
		onNodeWithText("Segunda").assertIsDisplayed()
	}

	private companion object {
		const val BodyTag = "rich_tooltip_lines_body"
	}
}
