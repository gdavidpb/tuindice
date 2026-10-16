package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.testing.summaryCareerProgressItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryCareerProgressCardViewUiTest {
	@Test
	fun when_itIsShown_then_itsCreditsAndItsPercentageAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCareerProgressCardView(career = summaryCareerProgressItem(), onClick = {})
		}

		onNodeWithText("de 240 créditos", substring = true).assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.CareerPercentText, useUnmergedTree = true).assertTextEquals("65 %")
	}

	@Test
	fun when_itIsTapped_then_itLeadsToPensumOnce() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryCareerProgressCardView(career = summaryCareerProgressItem(), onClick = { taps++ })
		}
		onNodeWithTag(SummaryUiTags.CareerCard).performClick()

		assertEquals(1, taps)
	}
}
