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
class EnrollmentProofLottieLoadingContentUiTest {
	@Test
	fun when_lottieLoadingContentRendered_then_displaysLottieNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofLottieLoadingContent()
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingLottie)
	}

	@Test
	fun when_lottieLoadingContentRendered_then_displaysSingleLottieNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofLottieLoadingContent()
		}

		onAllNodesWithTag(EnrollmentProofUiTags.FetchingLottie).assertCountEquals(1)
	}
}
