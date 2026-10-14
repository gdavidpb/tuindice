package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_approved_many
import tuindice.summary.generated.resources.share_term_card_career
import tuindice.summary.generated.resources.share_term_card_credits_many
import tuindice.summary.generated.resources.share_term_card_cumulative_label
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardUiTest {
	@Test
	fun when_theArtworkHasAnAuthor_then_itsCreditNamesTheWorkAndItsAuthor() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), background = ShareBackground.included)
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Laberinto Cromovegetal · Carlos Cruz-Diez")
	}

	@Test
	fun when_theArtworkHasNoAuthor_then_itsCreditIsOnlyItsTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), background = biblioteca())
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Biblioteca Central")
	}

	@Test
	fun when_aRemoteArtworkCannotBeLoaded_then_theCardTurnsToTheBundledOneCreditAndAll() = runTuIndiceUiTest {
		var readyCount = 0

		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), background = unreachable(), onArtReady = { readyCount++ })
		}

		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
			onAllNodesWithText("Laberinto Cromovegetal · Carlos Cruz-Diez").fetchSemanticsNodes().isNotEmpty()
		}
		onAllNodesWithText("Espejo Solar · Alejandro Otero").assertCountEquals(0)
		waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { readyCount == 1 }
	}

	@Test
	fun when_itIsShown_then_itShowsTheTermItsFiguresAndItsCareer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCard(item = termItem(), background = ShareBackground.included)
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
			ShareableTermCard(item = termItem(), background = ShareBackground.included)
		}

		onNodeWithTag(SummaryUiTags.ShareTermCard)
			.assertWidthIsEqualTo(360.dp)
			.assertHeightIsEqualTo(540.dp)
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

	// A remote artwork on a port nothing listens on, so its load fails at once.
	private fun unreachable() = ShareBackground(
		id = "espejo-solar",
		title = "Espejo Solar",
		author = "Alejandro Otero",
		shortName = "Espejo",
		art = ShareBackgroundArt.Remote(
			cardUrl = "http://127.0.0.1:9/espejo-solar/v1/card.webp",
			thumbUrl = "http://127.0.0.1:9/espejo-solar/v1/thumb.webp"
		)
	)

	private companion object {
		const val LOAD_TIMEOUT_MILLIS = 10_000L
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
