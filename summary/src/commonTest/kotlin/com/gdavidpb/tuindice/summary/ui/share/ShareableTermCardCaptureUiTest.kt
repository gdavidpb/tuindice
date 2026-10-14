package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_approved_many
import tuindice.summary.generated.resources.share_term_card_career
import tuindice.summary.generated.resources.share_term_card_credits_many
import tuindice.summary.generated.resources.share_term_card_cumulative_label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardCaptureUiTest {
	@Test
	fun when_theArtworkIsReady_then_theCardIsCapturedAtTheSharedSizeWithContent() = runTuIndiceUiTest {
		val captures = mutableListOf<ImageBitmap>()

		setTuIndiceTestContent {
			ShareableTermCardCapture(
				item = termItem(),
				background = ShareBackground.included,
				onCaptured = { bitmap -> captures += bitmap }
			)
		}
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { captures.isNotEmpty() }

		val bitmap = captures.single()
		assertEquals(1080, bitmap.width)
		assertEquals(1620, bitmap.height)

		val pixels = IntArray(bitmap.width * bitmap.height)
		bitmap.readPixels(pixels)
		assertTrue(pixels.any { pixel -> (pixel ushr 24) != 0 }, "The capture is empty.")
	}

	@Test
	fun when_itIsCapturedForABackground_then_itIsCapturedOnlyOnce() = runTuIndiceUiTest {
		val captures = mutableListOf<ImageBitmap>()

		setTuIndiceTestContent {
			ShareableTermCardCapture(
				item = termItem(),
				background = ShareBackground.included,
				onCaptured = { bitmap -> captures += bitmap }
			)
		}
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { captures.isNotEmpty() }
		waitForIdle()

		assertEquals(1, captures.size)
	}

	private companion object {
		// Generous on purpose: it only bounds a hang, and a CI runner composes far slower than a developer machine.
		const val CAPTURE_TIMEOUT_MILLIS = 5_000L
	}

	private fun termItem() = ShareableTermCardItem(
		term = UiText.Raw("Enero - Marzo 2026"),
		index = "3.6875",
		approved = SummaryStatisticsFigureItem(
			value = "3",
			label = UiText.Resource(Res.string.share_term_card_approved_many)
		),
		credits = SummaryStatisticsFigureItem(
			value = "12",
			label = UiText.Resource(Res.string.share_term_card_credits_many)
		),
		cumulative = SummaryStatisticsFigureItem(
			value = "4.1250",
			label = UiText.Resource(Res.string.share_term_card_cumulative_label)
		),
		career = UiText.Resource(
			Res.string.share_term_card_career,
			listOf("Ingeniería de Computación")
		)
	)
}
