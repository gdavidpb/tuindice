package com.gdavidpb.tuindice.enrollmentproof.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class EnrollmentProofScreenUiTest {
	@Test
	fun when_stateIsFetching_then_displaysFetchingView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentProofScreen(
				state = Enrollment.State.Fetching,
				onDismissRequest = {}
			)
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingSheet)
	}

	@Test
	fun when_stateIsFetching_then_doesNotInvokeDismissCallbackOnInitialRender() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			EnrollmentProofScreen(
				state = Enrollment.State.Fetching,
				onDismissRequest = { dismissCalls++ }
			)
		}

		assertNodeVisible(EnrollmentProofUiTags.FetchingSheet)
		assertEquals(0, dismissCalls)
	}
}
