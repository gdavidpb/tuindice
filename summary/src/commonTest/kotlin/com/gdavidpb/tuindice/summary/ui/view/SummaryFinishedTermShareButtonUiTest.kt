package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryFinishedTermShareButtonUiTest {
	@Test
	fun when_itIsShown_then_itIsDisplayed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFinishedTermShareButton(onClick = {})
		}

		onNodeWithTag(SummaryUiTags.TermFinishedShare).assertIsDisplayed()
	}

	@Test
	fun when_itIsTapped_then_theShareIsAskedForOnce() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryFinishedTermShareButton(onClick = { taps++ })
		}
		onNodeWithTag(SummaryUiTags.TermFinishedShare).performClick()

		assertEquals(1, taps)
	}
}
