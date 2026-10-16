package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareTermSheetLoadingUiTest {
	@Test
	fun when_itIsShown_then_itShowsTheLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermSheetLoading()
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermLoadingIndicator).assertCountEquals(1)
	}

	@Test
	fun when_theTermsAreStillRead_then_theIndicatorIsDisplayed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermSheetLoading()
		}

		onNodeWithTag(SummaryUiTags.ShareTermLoadingIndicator).assertIsDisplayed()
	}
}
