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
class EvaluationLoadingViewUiTest {
	@Test
	fun when_rendered_then_displaysLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationLoadingView()
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationLoadingIndicator)
	}

	@Test
	fun when_rendered_then_displaysSingleLoadingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationLoadingView()
		}

		onAllNodesWithTag(EvaluationsUiTags.EvaluationLoadingIndicator)
			.assertCountEquals(1)
	}
}
