package com.gdavidpb.tuindice.enrollmentproof.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EnrollmentProofFetchingViewUiTest {
	@Test
	fun when_fetchingViewRendered_then_displaysSheetAndLoadingContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofFetchingView(onDismissRequest = {})
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingSheet)
		assertNodeVisible(EnrollmentProofUiTags.FetchingLoadingContainer)
		assertNodeVisible(EnrollmentProofUiTags.FetchingLottie)
		onNodeWithText("Obteniendo comprobante…").assertIsDisplayed()
	}

	@Test
	fun when_fetchingViewRendered_then_doesNotInvokeDismissOnInitialRender() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofFetchingView(
				onDismissRequest = { dismissCalls++ }
			)
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingSheet)
		assertEquals(0, dismissCalls)
	}
}
