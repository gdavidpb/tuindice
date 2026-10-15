package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermPreviewUiTest {
	@Test
	fun when_theCardIsShown_then_itIsDrawnInThePreview() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermPreview(
				card = card(),
				artwork = ShareBackground.included.toShareTermArtwork()
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermPreview).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermCard).assertCountEquals(1)
	}

	@Test
	fun when_theCardIsShown_then_itCoversThePreviewWholeFromItsTopStart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermPreview(
				card = card(),
				artwork = ShareBackground.included.toShareTermArtwork()
			)
		}
		waitForIdle()

		val preview = onNodeWithTag(SummaryUiTags.ShareTermPreview).fetchSemanticsNode().boundsInRoot
		val shown = onNodeWithTag(SummaryUiTags.ShareTermCard, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertEquals(preview.left, shown.left, absoluteTolerance = EDGE_TOLERANCE_PX)
		assertEquals(preview.top, shown.top, absoluteTolerance = EDGE_TOLERANCE_PX)
		assertEquals(preview.right, shown.right, absoluteTolerance = EDGE_TOLERANCE_PX)
		assertEquals(preview.bottom, shown.bottom, absoluteTolerance = EDGE_TOLERANCE_PX)
	}

	@Test
	fun when_theTermChanges_then_thePreviewShowsTheFiguresOfTheNewTerm() = runTuIndiceUiTest {
		val chosen = mutableStateOf(card())
		val newer = card(year = 2023, periodCode = AcademicTermPeriod.SEP_DEC, index = 2.5)

		setTuIndiceTestContent {
			ShareTermPreview(
				card = chosen.value,
				artwork = ShareBackground.included.toShareTermArtwork()
			)
		}
		chosen.value = newer

		waitUntil(timeoutMillis = NODE_TIMEOUT_MILLIS) {
			runCatching { onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals(newer.index) }.isSuccess
		}
	}

	@Test
	fun when_theBackgroundChanges_then_thePreviewShowsItsCredit() = runTuIndiceUiTest {
		val chosen = mutableStateOf(ShareBackground.included)

		setTuIndiceTestContent {
			ShareTermPreview(
				card = card(),
				artwork = chosen.value.toShareTermArtwork()
			)
		}
		chosen.value = otherShareBackground

		waitUntil(timeoutMillis = NODE_TIMEOUT_MILLIS) {
			runCatching { onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Otra obra") }.isSuccess
		}
	}

	private fun card(
		year: Int = 2024,
		periodCode: AcademicTermPeriod = AcademicTermPeriod.JAN_MAR,
		index: Double = 4.0
	): ShareableTermCardItem = checkNotNull(
		statisticsTerm(year = year, periodCode = periodCode, index = index)
			.toShareableTermCardItem(careerName = "Ingeniería de Computación")
	)

	private companion object {
		const val NODE_TIMEOUT_MILLIS = 5_000L

		// The card is scaled to the preview's width, so its edges may land a pixel off the preview's.
		const val EDGE_TOLERANCE_PX = 2f
	}
}
