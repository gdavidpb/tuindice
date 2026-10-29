package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.testing.RecordingProfilePictureLoader
import com.gdavidpb.tuindice.summary.testing.v2SignedPictureUrl
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.ProfilePictureDisplay
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.CompletableDeferred
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

	// A sync writes the user again and the backend signs the same picture's URL again: what is on screen stays, with no
	// new download, no restarted request, no continuity layer and no loading indicator in any frame.
	@Test
	fun when_aSyncReSignsTheSamePicture_then_theShownPictureIsNotLoadedAgain() = runTuIndiceUiTest {
		val loader = RecordingProfilePictureLoader().also { it.install() }

		try {
			var display by mutableStateOf(ProfilePictureDisplay(url = v2SignedPictureUrl(expires = 1_000), cacheVersion = 1))

			setTuIndiceTestContent {
				ProfilePictureView(display = display, onClick = { })
			}

			awaitShownPicture(loader)

			val startedBeforeRotation = loader.startedRequests

			mainClock.autoAdvance = false
			display = display.copy(url = v2SignedPictureUrl(expires = 2_000))

			repeat(ROTATION_FRAMES) {
				mainClock.advanceTimeByFrame()

				assertNoNode(SummaryUiTags.ProfilePictureContinuityImage)
				assertNoNode(SummaryUiTags.ProfilePictureRemoteLoadingIndicator)
			}

			mainClock.autoAdvance = true
			waitForIdle()

			assertEquals(listOf(v2SignedPictureUrl(expires = 1_000)), loader.fetchedUrls)
			assertEquals(startedBeforeRotation, loader.startedRequests)
		} finally {
			loader.uninstall()
		}
	}

	// The URL the picture kept expired while nothing was cached: its failure moves the request to the URL the sync
	// brought, which the rotation alone never asked for.
	@Test
	fun when_theKeptUrlFailsAfterARotation_then_theNewestUrlIsLoaded() = runTuIndiceUiTest {
		val expiredUrl = v2SignedPictureUrl(expires = 1_000)
		val freshUrl = v2SignedPictureUrl(expires = 2_000)
		val expiry = CompletableDeferred<Unit>()
		val loader = RecordingProfilePictureLoader(failingUrls = setOf(expiredUrl), failingGate = expiry)
			.also { it.install() }

		try {
			var display by mutableStateOf(ProfilePictureDisplay(url = expiredUrl, cacheVersion = 1))

			setTuIndiceTestContent {
				ProfilePictureView(display = display, onClick = { })
			}

			waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { loader.fetchedUrls == listOf(expiredUrl) }

			display = display.copy(url = freshUrl)
			waitForIdle()

			assertEquals(listOf(expiredUrl), loader.fetchedUrls)

			expiry.complete(Unit)

			waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { loader.fetchedUrls == listOf(expiredUrl, freshUrl) }
			waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { !hasNode(SummaryUiTags.ProfilePictureRemoteLoadingIndicator) }
		} finally {
			loader.uninstall()
		}
	}

	private fun ComposeUiTest.awaitShownPicture(loader: RecordingProfilePictureLoader) {
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { loader.fetchedUrls.size == 1 }
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { !hasNode(SummaryUiTags.ProfilePictureRemoteLoadingIndicator) }
	}

	private fun ComposeUiTest.hasNode(tag: String): Boolean {
		return onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
	}

	private fun ComposeUiTest.assertNoNode(tag: String) {
		assertEquals(false, hasNode(tag), "$tag is on screen")
	}

	private companion object {
		const val LOAD_TIMEOUT_MILLIS = 5_000L
		const val ROTATION_FRAMES = 30
	}
}
