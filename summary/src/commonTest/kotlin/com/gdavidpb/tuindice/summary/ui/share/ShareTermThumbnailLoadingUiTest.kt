package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// The mark over the thumbnail waits before it shows, so a picture that is already on the phone never flashes it.
@OptIn(ExperimentalTestApi::class)
class ShareTermThumbnailLoadingUiTest {
	@Test
	fun when_theWaitIsShorterThanTheDelay_then_theMarkDoesNotShow() = runTuIndiceUiTest {
		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareTermThumbnailLoading(isLoading = true, modifier = Modifier.testTag(MARK_TAG))
		}

		advanceAnimationsBy(SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS - MARGIN_MILLIS)

		onAllNodesWithTag(MARK_TAG).assertCountEquals(0)
	}

	@Test
	fun when_theWaitOutlastsTheDelay_then_theMarkShowsAndSaysItIsLoading() = runTuIndiceUiTest {
		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareTermThumbnailLoading(isLoading = true, modifier = Modifier.testTag(MARK_TAG))
		}

		advanceAnimationsBy(SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS + MARGIN_MILLIS)

		onNodeWithTag(MARK_TAG).assertContentDescriptionEquals("Cargando fondo")
	}

	@Test
	fun when_theWaitEnds_then_theMarkGoesAtOnce() = runTuIndiceUiTest {
		val isLoading = mutableStateOf(true)

		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareTermThumbnailLoading(isLoading = isLoading.value, modifier = Modifier.testTag(MARK_TAG))
		}
		advanceAnimationsBy(SHARE_TERM_THUMBNAIL_LOADING_DELAY_MILLIS + MARGIN_MILLIS)
		onAllNodesWithTag(MARK_TAG).assertCountEquals(1)

		isLoading.value = false
		advanceAnimationsBy(ONE_FRAME_MILLIS)

		onAllNodesWithTag(MARK_TAG).assertCountEquals(0)
	}

	private companion object {
		const val MARK_TAG = "mark"
		const val MARGIN_MILLIS = 50L
		const val ONE_FRAME_MILLIS = 16L
	}
}
