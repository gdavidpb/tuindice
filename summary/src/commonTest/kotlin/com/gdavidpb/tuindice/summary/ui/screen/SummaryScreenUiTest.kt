package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryScreenUiTest {
	@Test
	fun when_stateIsLoading_then_displaysLoadingView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = Summary.State.Loading,
				onRetryClick = {},
				onEditProfilePictureClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.LoadingIndicator)
	}

	@Test
	fun when_stateIsFailedAndRetryTapped_then_invokesRetryCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SummaryScreen(
				state = Summary.State.Failed,
				onRetryClick = { retryClicks++ },
				onEditProfilePictureClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewContainer)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		assertEquals(1, retryClicks)
	}

	@Test
	fun when_stateIsContent_then_displaysSummaryContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				onRetryClick = {},
				onEditProfilePictureClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
	}

	@Test
	fun when_profilePictureEditTappedFromContent_then_invokesEditCallback() = runTuIndiceUiTest {
		var editClicks = 0

		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				onRetryClick = {},
				onEditProfilePictureClick = { editClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureEditButton)
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, editClicks)
	}
}
