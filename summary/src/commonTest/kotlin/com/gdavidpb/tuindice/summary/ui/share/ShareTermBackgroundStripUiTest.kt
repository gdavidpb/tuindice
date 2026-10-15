package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermBackgroundStripUiTest {
	@Test
	fun when_aBackgroundIsChosen_then_onlyItWearsTheRing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermBackgroundStrip(
				backgrounds = backgrounds(chosenId = ShareBackground.included.id),
				onBackgroundClick = {}
			)
		}

		onAllNodesWithTag(
			ShareTermUiTags.backgroundRing(ShareBackground.included.id),
			useUnmergedTree = true
		).assertCountEquals(1)
		onAllNodesWithTag(
			ShareTermUiTags.backgroundRing(otherShareBackground.id),
			useUnmergedTree = true
		).assertCountEquals(0)
	}

	@Test
	fun when_aBackgroundIsTapped_then_itIsReportedToTheSheet() = runTuIndiceUiTest {
		val tapped = mutableListOf<String>()

		setTuIndiceTestContent {
			ShareTermBackgroundStrip(
				backgrounds = backgrounds(chosenId = ShareBackground.included.id),
				onBackgroundClick = { backgroundId -> tapped += backgroundId }
			)
		}

		onNodeWithTag(ShareTermUiTags.background(otherShareBackground.id)).performClick()
		waitForIdle()

		assertEquals(listOf(otherShareBackground.id), tapped)
	}

	private fun backgrounds(chosenId: String) = listOf(
		ShareBackground.included,
		otherShareBackground
	).map { background ->
		ShareTermBackgroundItem(artwork = background.toShareTermArtwork(), selected = background.id == chosenId)
	}
}
