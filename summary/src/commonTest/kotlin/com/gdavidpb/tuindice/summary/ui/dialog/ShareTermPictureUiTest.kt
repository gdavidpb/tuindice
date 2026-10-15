package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermPictureUiTest {
	@Test
	fun when_theArtIsReady_then_thePictureIsHandedOnceToTheSheet() = runTuIndiceUiTest {
		var captured = 0

		setTuIndiceTestContent {
			ShareTermPicture(
				card = shareCardItem(),
				artwork = ShareBackground.included.toShareTermArtwork(),
				onCaptured = { captured++ }
			)
		}
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { captured > 0 }
		waitForIdle()

		assertEquals(1, captured)
	}

	@Test
	fun when_itIsComposed_then_theCardIsDrawnForThePicture() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermPicture(
				card = shareCardItem(),
				artwork = ShareBackground.included.toShareTermArtwork(),
				onCaptured = {}
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex, useUnmergedTree = true).assertCountEquals(1)
	}

	private companion object {
		const val CAPTURE_TIMEOUT_MILLIS = 10_000L
	}
}
