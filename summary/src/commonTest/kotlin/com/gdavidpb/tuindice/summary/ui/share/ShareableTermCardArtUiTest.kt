package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardArtUiTest {
	@Test
	fun when_theArtworkIsBundled_then_itIsReadyAtOnce() = runTuIndiceUiTest {
		var readyCount = 0

		setTuIndiceTestContent {
			ShareableTermCardArt(
				shown = ShareBackground.included.toShareTermArtwork(),
				onArtReady = { readyCount++ },
				onArtFailed = {}
			)
		}
		waitForIdle()

		assertEquals(1, readyCount)
	}

	@Test
	fun when_thereIsNoArtworkYet_then_nothingIsReady() = runTuIndiceUiTest {
		var readyCount = 0

		setTuIndiceTestContent {
			ShareableTermCardArt(shown = null, onArtReady = { readyCount++ }, onArtFailed = {})
		}
		waitForIdle()

		assertEquals(0, readyCount)
	}
}
