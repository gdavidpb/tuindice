package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationsLoadingViewUiTest {
	@Test
	fun when_rendered_then_displaysLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsLoadingView()
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationsLoadingIndicator)
	}

	@Test
	fun when_rendered_then_displaysSingleLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationsLoadingView()
		}

		onAllNodesWithTag(EvaluationsUiTags.EvaluationsLoadingIndicator)
			.assertCountEquals(1)
	}
}
