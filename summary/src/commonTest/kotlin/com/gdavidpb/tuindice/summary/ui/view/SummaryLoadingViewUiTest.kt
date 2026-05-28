package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryLoadingViewUiTest {
	@Test
	fun when_loadingViewIsRendered_then_displaysLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLoadingView()
		}

		assertNodeVisible(SummaryUiTags.LoadingIndicator)
	}

	@Test
	fun when_loadingViewIsRendered_then_displaysSingleIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLoadingView()
		}

		onAllNodesWithTag(SummaryUiTags.LoadingIndicator).assertCountEquals(1)
	}
}
