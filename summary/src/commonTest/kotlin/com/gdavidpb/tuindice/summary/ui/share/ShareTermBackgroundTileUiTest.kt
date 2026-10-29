package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.testing.bibliotecaArtwork
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermBackgroundTileUiTest {
	@Test
	fun when_theTileIsTapped_then_itsBackgroundIsReported() = runTuIndiceUiTest {
		val tapped = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			ShareTermBackgroundTile(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = false),
				onClick = { tapped += true }
			)
		}
		onNodeWithTag(ShareTermUiTags.background(BIBLIOTECA_ID)).performClick()

		assertEquals(listOf(true), tapped)
	}

	@Test
	fun when_itIsChosen_then_itWearsTheRingOfThePrimaryColour() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermBackgroundTile(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = true),
				onClick = {}
			)
		}

		onNodeWithTag(ShareTermUiTags.backgroundRing(BIBLIOTECA_ID), useUnmergedTree = true).assertExists()
	}

	@Test
	fun when_itIsNotChosen_then_itWearsNoRing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermBackgroundTile(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = false),
				onClick = {}
			)
		}

		onAllNodesWithTag(ShareTermUiTags.backgroundRing(BIBLIOTECA_ID)).assertCountEquals(0)
	}

	@Test
	fun when_itsPictureIsOnItsWay_then_itsThumbnailIsMarkedAsLoading() = runTuIndiceUiTest {
		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareTermBackgroundTile(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = true),
				onClick = {},
				isLoading = true
			)
		}

		advanceAnimationsBy(SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS + MARGIN_MILLIS)

		onNodeWithTag(ShareTermUiTags.backgroundLoading(BIBLIOTECA_ID), useUnmergedTree = true).assertExists()
	}

	@Test
	fun when_itsPictureIsNotOnItsWay_then_itsThumbnailIsNotMarked() = runTuIndiceUiTest {
		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareTermBackgroundTile(
				item = ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = true),
				onClick = {}
			)
		}

		advanceAnimationsBy(SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS + MARGIN_MILLIS)

		onAllNodesWithTag(ShareTermUiTags.backgroundLoading(BIBLIOTECA_ID), useUnmergedTree = true)
			.assertCountEquals(0)
	}

	private companion object {
		const val BIBLIOTECA_ID = "biblioteca-central"
		const val MARGIN_MILLIS = 50L
	}
}
