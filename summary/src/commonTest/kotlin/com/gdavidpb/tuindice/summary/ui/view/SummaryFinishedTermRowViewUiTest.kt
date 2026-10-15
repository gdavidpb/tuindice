package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryFinishedTermRowViewUiTest {
	@Test
	fun when_itIsShown_then_itsLabelAndItsValueAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFinishedTermRowView(
				label = "Índice",
				value = "3.6875",
				valueTag = SummaryUiTags.TermFinishedIndex
			)
		}

		onNodeWithText("Índice").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermFinishedIndex).assertTextEquals("3.6875")
	}

	@Test
	fun when_itIsTheCreditsLine_then_itsValueIsTheCreditsOne() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFinishedTermRowView(
				label = "Créditos",
				value = "120",
				valueTag = SummaryUiTags.TermFinishedCredits
			)
		}

		onNodeWithTag(SummaryUiTags.TermFinishedCredits).assertTextEquals("120")
	}
}
