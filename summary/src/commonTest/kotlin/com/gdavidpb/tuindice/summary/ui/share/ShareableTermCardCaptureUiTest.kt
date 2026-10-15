package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.testing.unreachableShareBackground
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
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
				artwork = ShareBackground.included.toShareTermArtwork(),
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

		// The middle of the card lies clear of both fades, so the artwork shows there: the bone alone has one colour.
		val colours = HashSet<Int>()
		for (row in ARTWORK_ROWS) {
			for (column in 0 until bitmap.width) {
				colours += pixels[row * bitmap.width + column]
			}
		}
		assertTrue(colours.size > MIN_ARTWORK_COLOURS, "The artwork is not in the picture.")
	}

	@Test
	fun when_itIsCapturedForABackground_then_itIsCapturedOnlyOnce() = runTuIndiceUiTest {
		val captures = mutableListOf<ImageBitmap>()

		setTuIndiceTestContent {
			ShareableTermCardCapture(
				item = termItem(),
				artwork = ShareBackground.included.toShareTermArtwork(),
				onCaptured = { bitmap -> captures += bitmap }
			)
		}
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { captures.isNotEmpty() }
		waitForIdle()

		assertEquals(1, captures.size)
	}

	@Test
	fun when_aRemoteArtworkCannotBeLoaded_then_theCaptureIsTheCardAtTheSharedSize() = runTuIndiceUiTest {
		val captures = mutableListOf<ImageBitmap>()

		setTuIndiceTestContent {
			ShareableTermCardCapture(
				item = termItem(),
				artwork = unreachableShareBackground.toShareTermArtwork(),
				onCaptured = { bitmap -> captures += bitmap }
			)
		}
		waitUntil(timeoutMillis = REMOTE_TIMEOUT_MILLIS) { captures.isNotEmpty() }

		val bitmap = captures.single()
		assertEquals(1080, bitmap.width)
		assertEquals(1620, bitmap.height)
	}

	@Test
	fun when_theTermChangesWhileItIsCaptured_then_thereIsOneCardAtOnce() = runTuIndiceUiTest {
		val item = mutableStateOf(termItem(index = "3.6875"))

		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareableTermCardCapture(
				item = item.value,
				artwork = ShareBackground.included.toShareTermArtwork(),
				onCaptured = {}
			)
		}
		item.value = termItem(index = "2.5000")
		repeat(FRAMES_UNTIL_THE_CHANGE_SHOWS) {
			advanceAnimationsBy(ONE_FRAME_MILLIS)
		}

		// The picture is taken still, so the term before is not drawn under the new one: there is one card at once.
		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex).assertCountEquals(1)
		onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals("2.5000")
	}

	private companion object {
		// Generous on purpose: it only bounds a hang, and a CI runner composes far slower than a developer machine.
		const val CAPTURE_TIMEOUT_MILLIS = 5_000L

		// The clock of a test frame by frame: one frame at 60 Hz.
		const val ONE_FRAME_MILLIS = 16L

		// A change reaches the card a frame or two after it is made, and the card it fades to a frame later still.
		const val FRAMES_UNTIL_THE_CHANGE_SHOWS = 4

		// A remote artwork is asked for before the card is captured, so its failure is waited for as well.
		const val REMOTE_TIMEOUT_MILLIS = 10_000L

		// Between the top fade and the bottom one, on a card of 1620 pixels.
		val ARTWORK_ROWS = 900 until 1200
		const val MIN_ARTWORK_COLOURS = 100
	}

	private fun termItem(index: String = "3.6875") = ShareableTermCardItem(
		term = UiText.Raw("Enero - Marzo 2026"),
		index = index,
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
