package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.ProfilePictureDisplay
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ProfilePictureViewUiTest {
	@Test
	fun when_pictureIsIdleAndContainerTapped_then_invokesClickCallback() = runTuIndiceUiTest {
		var pictureClicks = 0

		setTuIndiceTestContent {
			ProfilePictureView(
				display = ProfilePictureDisplay(url = ""),
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
				display = ProfilePictureDisplay(url = ""),
				onClick = { pictureClicks++ }
			)
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, pictureClicks)
	}

	@Test
	fun when_pictureIsUploading_then_disablesEditButtonAndShowsButtonLoader() = runTuIndiceUiTest {
		var pictureClicks = 0

		setTuIndiceTestContent {
			ProfilePictureView(
				display = ProfilePictureDisplay(
					url = "https://tuindice.test/profile.jpg",
					isUploading = true
				),
				onClick = { pictureClicks++ }
			)
		}

		assertNodeVisible(
			tag = SummaryUiTags.ProfilePictureLoadingIndicator,
			useUnmergedTree = true
		)
		assertNodeDisabled(SummaryUiTags.ProfilePictureContainer)
		assertNodeDisabled(SummaryUiTags.ProfilePictureEditButton)

		assertEquals(0, pictureClicks)
	}

	@Test
	fun when_pictureInteractionIsDisabled_then_disablesContainerAndEditButtonWithoutLoader() = runTuIndiceUiTest {
		var pictureClicks = 0

		setTuIndiceTestContent {
			ProfilePictureView(
				isEnabled = false,
				display = ProfilePictureDisplay(url = "https://tuindice.test/profile.jpg"),
				onClick = { pictureClicks++ }
			)
		}

		assertNodeDisabled(SummaryUiTags.ProfilePictureContainer)
		assertNodeDisabled(SummaryUiTags.ProfilePictureEditButton)
		assertEquals(0, pictureClicks)
	}

	@Test
	fun when_uploadPreviewIsSet_then_showsLocalPreviewLayer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureView(
				display = ProfilePictureDisplay(
					url = "https://tuindice.test/profile.jpg",
					localPreviewPath = "/tmp/profile-preview.jpg",
					isUploading = true
				),
				onClick = { }
			)
		}

		assertNodeVisible(
			tag = SummaryUiTags.ProfilePicturePreviewImage,
			useUnmergedTree = true
		)
		assertNodeVisible(
			tag = SummaryUiTags.ProfilePictureLoadingIndicator,
			useUnmergedTree = true
		)
		assertNodeDisabled(SummaryUiTags.ProfilePictureContainer)
	}
}
