package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ProfilePictureViewUiTest {
	@Test
	fun when_pictureIsIdleAndContainerTapped_then_invokesClickCallback() = runTuIndiceUiTest {
		var pictureClicks = 0

		setTuIndiceTestContent {
			ProfilePictureView(
				state = ProfilePictureState(
					url = "",
					isLoading = false
				),
				onLoading = {},
				onClick = { pictureClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureContainer)
		assertNodeVisible(
			tag = SummaryUiTags.ProfilePicturePlaceholderIcon,
			useUnmergedTree = true
		)
		assertNodeVisible(SummaryUiTags.ProfilePictureEditButton)

		onNodeWithTag(SummaryUiTags.ProfilePictureContainer).performClick()

		assertEquals(1, pictureClicks)
	}

	@Test
	fun when_pictureIsIdleAndEditButtonTapped_then_invokesClickCallback() = runTuIndiceUiTest {
		var pictureClicks = 0

		setTuIndiceTestContent {
			ProfilePictureView(
				state = ProfilePictureState(
					url = "",
					isLoading = false
				),
				onLoading = {},
				onClick = { pictureClicks++ }
			)
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, pictureClicks)
	}

	@Test
	fun when_pictureIsLoading_then_disablesEditButtonAndShowsLoader() = runTuIndiceUiTest {
		var pictureClicks = 0

		setTuIndiceTestContent {
			ProfilePictureView(
				state = ProfilePictureState(
					url = "https://tuindice.test/profile.jpg",
					isLoading = true
				),
				onLoading = {},
				onClick = { pictureClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureLoadingIndicator)
		assertNodeDisabled(SummaryUiTags.ProfilePictureContainer)
		assertNodeDisabled(SummaryUiTags.ProfilePictureEditButton)

		assertEquals(0, pictureClicks)
	}

	@Test
	fun when_pictureUrlIsBlank_then_notifiesLoadingAsFalse() = runTuIndiceUiTest {
		val loadingEvents = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			ProfilePictureView(
				state = ProfilePictureState(
					url = "",
					isLoading = true
				),
				onLoading = { isLoading -> loadingEvents += isLoading },
				onClick = {}
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			loadingEvents.isNotEmpty()
		}

		assertTrue(loadingEvents.contains(false))
	}
}
