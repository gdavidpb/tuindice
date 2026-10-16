package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardUiTest {
	@Test
	fun when_theArtworkHasAnAuthor_then_itsCreditNamesTheWorkAndItsAuthor() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), artwork = ShareBackground.included.toShareTermArtwork())
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals(INCLUDED_CREDIT)
	}

	@Test
	fun when_theArtworkHasNoAuthor_then_itsCreditIsOnlyItsTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), artwork = biblioteca().toShareTermArtwork())
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Biblioteca Central")
	}

	@Test
	fun when_aRemoteArtworkCannotBeLoaded_then_itIsNeverDrawnAndTheCardTurnsToTheBundledOne() = runTuIndiceUiTest {
		var readyCount = 0
		var drewTheUnloadedCredit = false

		setTuIndiceTestContent {
			ShareableTermCard(
				item = termItem(),
				artwork = unreachableShareBackground.toShareTermArtwork(),
				onArtReady = { readyCount++ }
			)
		}

		// Frame by frame until the card has turned: the artwork that cannot be loaded is never on screen.
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
			drewTheUnloadedCredit = drewTheUnloadedCredit ||
				onAllNodesWithText(UNLOADED_CREDIT).fetchSemanticsNodes().isNotEmpty()

			onAllNodesWithText(INCLUDED_CREDIT).fetchSemanticsNodes().isNotEmpty()
		}

		assertFalse(drewTheUnloadedCredit, "The card drew the artwork that could not be loaded.")
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readyCount == 1 }
	}

	@Test
	fun when_aRemoteArtworkIsChosen_then_itIsReportedPendingUntilTheCardSettlesOnIt() = runTuIndiceUiTest {
		val background = mutableStateOf(ShareBackground.included)
		val reported = mutableListOf<String?>()

		setTuIndiceTestContent {
			ShareableTermCard(
				item = termItem(),
				artwork = background.value.toShareTermArtwork(),
				onArtworkPending = { artworkId -> reported += artworkId }
			)
		}
		waitForIdle()
		background.value = unreachableShareBackground

		// The picture cannot be loaded, so the card settles on the bundled one: the wait ends there too.
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
			reported.contains(unreachableShareBackground.id) && reported.last() == null
		}

		assertEquals(listOf(null, unreachableShareBackground.id, null), reported)
	}

	@Test
	fun when_theBundledArtworkIsShown_then_nothingIsReportedPending() = runTuIndiceUiTest {
		val reported = mutableListOf<String?>()

		setTuIndiceTestContent {
			ShareableTermCard(
				item = termItem(),
				artwork = ShareBackground.included.toShareTermArtwork(),
				onArtworkPending = { artworkId -> reported += artworkId }
			)
		}
		waitForIdle()

		assertEquals(listOf<String?>(null), reported)
	}

	@Test
	fun when_itIsShown_then_itShowsTheTermItsFiguresAndItsCareer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), artwork = ShareBackground.included.toShareTermArtwork())
		}

		onNodeWithText("Mi trimestre").assertIsDisplayed()
		onNodeWithText("Enero - Marzo 2026").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals("3.6875")
		onNodeWithText("3").assertIsDisplayed()
		onNodeWithText("aprobadas").assertIsDisplayed()
		onNodeWithText("12").assertIsDisplayed()
		onNodeWithText("créditos").assertIsDisplayed()
		onNodeWithText("4.1250").assertIsDisplayed()
		onNodeWithText("acumulado").assertIsDisplayed()
		onNodeWithText("Ingeniería de Computación · USB").assertIsDisplayed()
		onNodeWithText("TuIndice").assertIsDisplayed()
		// The university's logo signs the header: a missing drawable would measure nothing.
		val logo = onNodeWithTag(SummaryUiTags.ShareTermCardLogo, useUnmergedTree = true).fetchSemanticsNode()
		assertTrue(logo.size.width > 0 && logo.size.height > 0)
	}

	@Test
	fun when_itIsShown_then_itKeepsItsSharedSizeWhateverTheScreen() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 2.625f) {
			ShareableTermCard(item = termItem(), artwork = ShareBackground.included.toShareTermArtwork())
		}

		onNodeWithTag(SummaryUiTags.ShareTermCard)
			.assertWidthIsEqualTo(360.dp)
			.assertHeightIsEqualTo(540.dp)
	}

	@Test
	fun when_theTermChangesWithoutAnimations_then_itsFiguresChangeAtOnce() = runTuIndiceUiTest {
		val item = mutableStateOf(termItem(index = "3.6875"))

		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				ShareableTermCard(item = item.value, artwork = ShareBackground.included.toShareTermArtwork())
			}
		}
		item.value = termItem(index = "2.5000")
		// With no animation the figures of the term before are gone at once: a few frames let the change reach the card.
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		onAllNodesWithText("3.6875").assertCountEquals(0)
		onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals("2.5000")
	}

	@Test
	fun when_theTermChangesWithAnimations_then_theWholeCardFadesAndNothingInItMoves() = runTuIndiceUiTest {
		val item = mutableStateOf(termItem(index = "3.6875"))

		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareableTermCard(item = item.value, artwork = ShareBackground.included.toShareTermArtwork())
		}
		val start = onNodeWithTag(SummaryUiTags.ShareTermCardIndex).fetchSemanticsNode().boundsInRoot
		item.value = termItem(index = "2.5000")
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		// Mid-fade the whole card of the term before is still on screen under the new one: its header too.
		onAllNodesWithText("Mi trimestre").assertCountEquals(2)
		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex).assertCountEquals(2)
		assertIndexAt(start)

		advanceAnimationsBy(TuIndiceAnimation.CrossfadeMillis.toLong() / 2)
		assertIndexAt(start)

		advanceAnimationsBy(FADE_WITH_MARGIN_MILLIS)

		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex).assertCountEquals(1)
		onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals("2.5000")
		assertIndexAt(start)
	}

	@Test
	fun when_theBackgroundChangesWithAnimations_then_theCreditFadesWithTheWholeCard() = runTuIndiceUiTest {
		val background = mutableStateOf(ShareBackground.included)

		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), artwork = background.value.toShareTermArtwork())
		}
		background.value = otherShareBackground
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		// Mid-fade the credit before and the credit it fades to are both on screen, and the figures of the card with them.
		onAllNodesWithText(INCLUDED_CREDIT).assertCountEquals(1)
		onAllNodesWithText(OTHER_CREDIT).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex).assertCountEquals(2)

		advanceAnimationsBy(FADE_WITH_MARGIN_MILLIS)

		onAllNodesWithText(INCLUDED_CREDIT).assertCountEquals(0)
		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals(OTHER_CREDIT)
		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex).assertCountEquals(1)
	}

	@Test
	fun when_theBackgroundChangesWithoutAnimations_then_itChangesAtOnce() = runTuIndiceUiTest {
		val background = mutableStateOf(ShareBackground.included)

		mainClock.autoAdvance = false
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				ShareableTermCard(item = termItem(), artwork = background.value.toShareTermArtwork())
			}
		}
		background.value = otherShareBackground
		advanceFrames(FRAMES_UNTIL_THE_CHANGE_SHOWS)

		onAllNodesWithText(INCLUDED_CREDIT).assertCountEquals(0)
		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals(OTHER_CREDIT)
	}

	private fun ComposeUiTest.advanceFrames(frames: Int) {
		repeat(frames) {
			advanceAnimationsBy(ONE_FRAME_MILLIS)
		}
	}

	// Every index on screen, the one before and the one faded to, sits where the index sat before the change.
	private fun ComposeUiTest.assertIndexAt(expected: Rect) {
		onAllNodesWithTag(SummaryUiTags.ShareTermCardIndex).fetchSemanticsNodes().forEach { index ->
			assertEquals(expected.left, index.boundsInRoot.left, absoluteTolerance = POSITION_TOLERANCE_PX)
			assertEquals(expected.top, index.boundsInRoot.top, absoluteTolerance = POSITION_TOLERANCE_PX)
		}
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

	private companion object {
		const val LOAD_TIMEOUT_MILLIS = 10_000L

		// The clock of a test frame by frame: one frame at 60 Hz.
		const val ONE_FRAME_MILLIS = 16L

		// A change reaches the card a frame after it is made, and the card it fades to is composed a frame or two later:
		// four frames let the change reach the screen, with its fade begun, before a test looks at the card.
		const val FRAMES_UNTIL_THE_CHANGE_SHOWS = 4

		// The fade ends a little after its duration, on the frame after the one it is due on.
		const val FADE_WITH_MARGIN_MILLIS = TuIndiceAnimation.CrossfadeMillis + 2L * ONE_FRAME_MILLIS

		const val POSITION_TOLERANCE_PX = 0.5f

		const val INCLUDED_CREDIT = "Laberinto Cromovegetal · Carlos Cruz-Diez"
		const val UNLOADED_CREDIT = "Espejo Solar · Alejandro Otero"
		const val OTHER_CREDIT = "Otra obra"
	}

	// The bundled artwork keeps this test off the network: only the credit of a work without an author is under test.
	private fun biblioteca() = ShareBackground(
		id = "biblioteca-central",
		title = "Biblioteca Central",
		author = null,
		shortName = "Biblioteca",
		art = ShareBackgroundArt.Bundled
	)
}
