package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecordLoadingViewUiTest {
	@Test
	fun when_loadingViewIsRendered_then_displaysLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordLoadingView()
		}

		assertNodeVisible(RecordUiTags.LoadingIndicator)
	}

	@Test
	fun when_loadingViewIsRendered_then_displaysSingleLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordLoadingView()
		}

		onAllNodesWithTag(RecordUiTags.LoadingIndicator).assertCountEquals(1)
	}
}
