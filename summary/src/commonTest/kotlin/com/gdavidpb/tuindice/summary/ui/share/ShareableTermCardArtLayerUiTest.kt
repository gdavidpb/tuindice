package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.testing.unreachableShareBackground
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardArtLayerUiTest {
	@Test
	fun when_theArtworkIsBundled_then_itIsReadyAtOnce() = runTuIndiceUiTest {
		var readyCount = 0

		setTuIndiceTestContent {
			ShareableTermCardArtLayer(
				artwork = ShareBackground.included.toShareTermArtwork(),
				onArtReady = { readyCount++ },
				onArtFailed = {}
			)
		}
		waitForIdle()

		assertEquals(1, readyCount)
	}

	@Test
	fun when_aRemoteArtworkCannotBeLoaded_then_itIsReportedAsFailed() = runTuIndiceUiTest {
		var failed: ShareTermArtwork? = null
		val remote = unreachableShareBackground.toShareTermArtwork()

		setTuIndiceTestContent {
			ShareableTermCardArtLayer(
				artwork = remote,
				onArtReady = {},
				onArtFailed = { artwork -> failed = artwork }
			)
		}
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { failed != null }

		assertEquals(remote.id, failed?.id)
	}

	private companion object {
		const val LOAD_TIMEOUT_MILLIS = 10_000L
	}
}
