package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsFactsViewUiTest {
	@Test
	fun when_factsAreGiven_then_eachValueAndItsLabelAreWrittenInOrderFromLeftToRight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsFactsView(facts = facts())
		}

		onNodeWithText("44 / 46").assertIsDisplayed()
		onNodeWithText("al primer intento").assertIsDisplayed()
		onNodeWithText("0").assertIsDisplayed()
		onNodeWithText("repetidas").assertIsDisplayed()
		onNodeWithText("2").assertIsDisplayed()
		onNodeWithText("sin resultado").assertIsDisplayed()
		assertTrue(
			onNodeWithText("44 / 46").fetchSemanticsNode().boundsInRoot.left <
				onNodeWithText("sin resultado").fetchSemanticsNode().boundsInRoot.left
		)
	}

	@Test
	fun when_noFactIsGiven_then_nothingIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsFactsView(facts = emptyList())
		}

		onNodeWithTag(SummaryUiTags.StatisticsFacts).assertDoesNotExist()
	}

	private fun facts(): List<SummaryStatisticsFactItem> {
		return listOf(
			SummaryStatisticsFactItem(value = "44 / 46", label = words("al primer intento")),
			SummaryStatisticsFactItem(value = "0", label = words("repetidas")),
			SummaryStatisticsFactItem(value = "2", label = words("sin resultado"))
		)
	}
}
