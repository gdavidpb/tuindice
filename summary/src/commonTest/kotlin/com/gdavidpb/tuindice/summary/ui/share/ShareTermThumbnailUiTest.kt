package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareTermThumbnailUiTest {
	@Test
	fun when_theArtIsBundled_then_itIsDrawnInItsTile() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.size(58.dp, 87.dp)) {
				ShareTermThumbnail(art = ShareTermArt.Bundled)
			}
		}

		onRoot(useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_theArtIsRemoteAndUnreachable_then_theTileStaysDrawnWithItsSurface() = runTuIndiceUiTest {
		val remote = ShareBackground.included.copy(
			id = "remoto",
			art = ShareBackgroundArt.Remote(
				cardUrl = "https://invalid.example/card.png",
				thumbUrl = "https://invalid.example/thumb.png"
			)
		).toShareTermArtwork()

		setTuIndiceTestContent {
			Box(modifier = Modifier.size(58.dp, 87.dp)) {
				ShareTermThumbnail(art = remote.art)
			}
		}
		waitForIdle()

		onRoot(useUnmergedTree = true).assertIsDisplayed()
	}
}
