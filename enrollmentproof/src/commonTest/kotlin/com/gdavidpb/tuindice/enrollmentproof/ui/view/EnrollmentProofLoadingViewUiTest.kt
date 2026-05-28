package com.gdavidpb.tuindice.enrollmentproof.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EnrollmentProofLoadingViewUiTest {
	@Test
	fun when_loadingViewRendered_then_displaysLottieNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofLoadingView()
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingLottie)
	}

	@Test
	fun when_loadingViewRendered_then_displaysSingleLottieNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofLoadingView()
		}

		onAllNodesWithTag(EnrollmentProofUiTags.FetchingLottie).assertCountEquals(1)
	}
}
