package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.RecordingProfilePictureLoader
import com.gdavidpb.tuindice.summary.testing.v2SignedPictureUrl
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ProfileAvatarViewUiTest {
	@Test
	fun when_theStudentHasNoPicture_then_showsOnlyTheNeutralPersonIcon() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfileAvatarView(pictureUrl = "", pictureVersion = 0, size = 32.dp)
		}

		assertNodeVisible(SummaryUiTags.ProfileAvatarFallback, useUnmergedTree = true)
		assertNodeHidden(SummaryUiTags.ProfileAvatarImage, useUnmergedTree = true)
	}

	@Test
	fun when_theStudentHasAPicture_then_theImageIsComposedOverTheFallback() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfileAvatarView(
				pictureUrl = "https://cdn.tuindice.app/profile/original.jpg",
				pictureVersion = 2,
				size = 32.dp
			)
		}

		assertNodeVisible(SummaryUiTags.ProfileAvatarFallback, useUnmergedTree = true)
		assertNodeVisible(SummaryUiTags.ProfileAvatarImage, useUnmergedTree = true)
	}

	// A sync signs the same picture's URL again: the avatar neither downloads it again nor restarts its request, which
	// would leave the person icon uncovered while it loads.
	@Test
	fun when_aSyncReSignsTheSamePicture_then_theFallbackIsNotUncovered() = runTuIndiceUiTest {
		val loader = RecordingProfilePictureLoader().also { it.install() }

		try {
			var pictureUrl by mutableStateOf(v2SignedPictureUrl(expires = 1_000))

			setTuIndiceTestContent {
				ProfileAvatarView(pictureUrl = pictureUrl, pictureVersion = 1, size = 32.dp)
			}

			waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { loader.fetchedUrls.size == 1 }
			waitForIdle()

			val startedBeforeRotation = loader.startedRequests

			pictureUrl = v2SignedPictureUrl(expires = 2_000)
			waitForIdle()

			assertEquals(startedBeforeRotation, loader.startedRequests)
			assertEquals(listOf(v2SignedPictureUrl(expires = 1_000)), loader.fetchedUrls)
			assertNodeVisible(SummaryUiTags.ProfileAvatarImage, useUnmergedTree = true)
		} finally {
			loader.uninstall()
		}
	}

	private companion object {
		const val LOAD_TIMEOUT_MILLIS = 5_000L
	}
}
