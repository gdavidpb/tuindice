package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.presentation.mapper.resolveSyncAttention
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The probe names the header when it composes. The sync icon turns on every frame of a running sync, and its turn is
// read in the icon's layer: the frames of the turn redraw the icon, and compose none of the header.
@OptIn(ExperimentalTestApi::class)
class SummaryContentCompositionUiTest {
	@Test
	fun when_syncIsRunning_then_theIconTurnsFrameByFrameWithoutRecomposingTheHeader() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalTuIndiceCompositionProbe provides probe,
				LocalTuIndiceAnimationsEnabled provides true
			) {
				SummaryContentView(
					state = summaryContentState(hub = summaryHubItem()),
					syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
					isSyncing = true,
					hubActions = SummaryHubActions(),
					onEditProfilePictureClick = {},
					onStatusIconClick = {}
				)
			}
		}

		// The first composition and its first frame come before the turn: they are not part of it.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val started = compositions.count { it == "summary-content" }
		assertTrue(started > 0, "the header composes")

		repeat(30) { mainClock.advanceTimeByFrame() }
		waitForIdle()

		assertEquals(
			expected = started,
			actual = compositions.count { it == "summary-content" },
			message = "the frames of the turn compose no header"
		)
	}

	// Once the sync stops, the icon finishes its turn and rests with no turn at all, and the header stops composing.
	@Test
	fun when_syncStops_then_theIconRestsWithoutATurnAndTheHeaderStopsComposing() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		var isSyncing by mutableStateOf(false)
		val headerIcon = { onNodeWithTag(SummaryUiTags.StatusIcon, useUnmergedTree = true).captureToImage().toPixelMap() }
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			HeaderWithReferenceIcon(isSyncing = isSyncing, probe = probe)
		}

		// Before any sync, the header icon already shows the picture of no turn.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val reference = onNodeWithTag(ReferenceTag).captureToImage().toPixelMap()
		assertTrue(inkPixels(reference) > 0, "the reference icon paints")
		assertEquals(
			expected = 0,
			actual = differingPixels(reference, headerIcon()),
			message = "before any sync the icon rests with no turn"
		)

		// While the sync runs, the icon turns: over the frames it stops painting the picture of no turn.
		isSyncing = true
		waitForIdle()
		var turnedSamples = 0
		repeat(TurnSamples) {
			repeat(TurnSampleFrames) { mainClock.advanceTimeByFrame() }
			waitForIdle()
			if (differingPixels(reference, headerIcon()) > 0) turnedSamples++
		}
		assertTrue(turnedSamples > 0, "the icon turns while the sync runs")

		// The sync stops: the icon finishes its turn and rests with no turn at all, as it did before the sync.
		isSyncing = false
		waitForIdle()
		repeat(StopFrames) { mainClock.advanceTimeByFrame() }
		waitForIdle()
		assertEquals(
			expected = 0,
			actual = differingPixels(reference, headerIcon()),
			message = "the stopped icon rests with no turn"
		)

		// Once it rests, no frame composes the header again.
		val restingCompositions = compositions.count { it == "summary-content" }
		repeat(CompositionFrames) { mainClock.advanceTimeByFrame() }
		waitForIdle()
		assertEquals(
			expected = restingCompositions,
			actual = compositions.count { it == "summary-content" },
			message = "a resting icon composes no header"
		)
	}

	// The header, and beside it the same sync icon drawn with no turn at all: the picture the header icon must rest on.
	@Composable
	private fun HeaderWithReferenceIcon(
		isSyncing: Boolean,
		probe: (String) -> Unit
	) {
		CompositionLocalProvider(
			LocalTuIndiceCompositionProbe provides probe,
			LocalTuIndiceAnimationsEnabled provides true
		) {
			Box(modifier = Modifier.fillMaxSize()) {
				SummaryContentView(
					state = summaryContentState(hub = summaryHubItem()),
					syncAttention = resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()),
					isSyncing = isSyncing,
					hubActions = SummaryHubActions(),
					onEditProfilePictureClick = {},
					onStatusIconClick = {}
				)
				Icon(
					modifier = Modifier
						.size(20.dp)
						.align(Alignment.TopEnd)
						.testTag(ReferenceTag),
					imageVector = Icons.Outlined.Sync,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					contentDescription = null
				)
			}
		}
	}

	// Pixels that differ between two pictures; a picture of another size differs in all of its pixels.
	private fun differingPixels(first: PixelMap, second: PixelMap): Int {
		if (first.width != second.width || first.height != second.height) return first.width * first.height
		var count = 0
		for (x in 0 until first.width) {
			for (y in 0 until first.height) {
				if (first[x, y] != second[x, y]) count++
			}
		}
		return count
	}

	// Pixels that differ from the corner, so a blank picture counts none.
	private fun inkPixels(picture: PixelMap): Int {
		val background = picture[0, 0]
		var count = 0
		for (x in 0 until picture.width) {
			for (y in 0 until picture.height) {
				if (picture[x, y] != background) count++
			}
		}
		return count
	}

	private companion object {
		const val ReferenceTag = "summary_reference_sync_icon"
		const val TurnSamples = 10
		const val TurnSampleFrames = 7
		const val StopFrames = 90
		const val CompositionFrames = 30
	}
}
