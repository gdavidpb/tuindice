package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardFooterUiTest {
	@Test
	fun when_theArtworkIsKnown_then_itsCreditAndTheCareerAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box {
				ShareableTermCardFooter(
					item = shareCardItem(),
					artwork = ShareBackground.included.toShareTermArtwork()
				)
			}
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals("Laberinto Cromovegetal · Carlos Cruz-Diez")
		onNodeWithText("Ingeniería de Computación · USB").assertIsDisplayed()
	}

	@Test
	fun when_theArtworkIsNotKnownYet_then_theCreditHoldsItsPlaceAndTheBrandStays() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box { ShareableTermCardFooter(item = shareCardItem(), artwork = null) }
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermCardCredit).assertCountEquals(1)
		onNodeWithText("TuIndice").assertIsDisplayed()
	}
}
