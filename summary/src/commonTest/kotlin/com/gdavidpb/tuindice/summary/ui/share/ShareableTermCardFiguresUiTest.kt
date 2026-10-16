package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardFiguresUiTest {
	@Test
	fun when_itIsShown_then_itShowsTheIndexAndTheThreeFigures() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box { ShareableTermCardFigures(item = shareCardItem()) }
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals("3.6875")
		onNodeWithText("aprobadas").assertIsDisplayed()
		onNodeWithText("créditos").assertIsDisplayed()
		onNodeWithText("acumulado").assertIsDisplayed()
	}

	@Test
	fun when_itIsShownForAnotherTerm_then_itShowsItsIndex() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box { ShareableTermCardFigures(item = shareCardItem(index = "2.5000")) }
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals("2.5000")
	}
}
