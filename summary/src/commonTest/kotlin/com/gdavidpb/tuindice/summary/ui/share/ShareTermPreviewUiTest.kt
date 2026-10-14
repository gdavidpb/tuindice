package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareableTermCardItem
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermPreviewUiTest {
	@Test
	fun when_theArtworkIsReady_then_theCardIsCapturedAtItsSharedSize() = runTuIndiceUiTest {
		var captured: ImageBitmap? = null

		setTuIndiceTestContent {
			ShareTermPreview(
				card = card(),
				background = ShareBackground.included,
				onCaptured = { bitmap -> captured = bitmap }
			)
		}

		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { captured != null }

		// 360 by 540 dp at three pixels per dp: the same 1080 pixels wide as the card that is shared.
		assertEquals(CARD_WIDTH_PIXELS, checkNotNull(captured).width)
	}

	@Test
	fun when_theCardIsShown_then_itIsDrawnInThePreview() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermPreview(
				card = card(),
				background = ShareBackground.included,
				onCaptured = {}
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermPreview).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermCard).assertCountEquals(1)
	}

	private fun card() = checkNotNull(
		statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.0)
			.toShareableTermCardItem(careerName = "Ingeniería de Computación")
	)

	private companion object {
		const val CARD_WIDTH_PIXELS = 1080
		const val CAPTURE_TIMEOUT_MILLIS = 5_000L
	}
}
