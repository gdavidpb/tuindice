package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.testing.bibliotecaArtwork
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardCreditUiTest {
	@Test
	fun when_theWorkHasAnAuthor_then_itsCreditNamesTheWorkAndItsAuthor() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCardCredit(artwork = ShareBackground.included.toShareTermArtwork())
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Laberinto Cromovegetal · Carlos Cruz-Diez")
	}

	@Test
	fun when_theWorkHasNoAuthor_then_itsCreditIsOnlyItsTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareableTermCardCredit(artwork = bibliotecaArtwork())
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Biblioteca Central")
	}
}
