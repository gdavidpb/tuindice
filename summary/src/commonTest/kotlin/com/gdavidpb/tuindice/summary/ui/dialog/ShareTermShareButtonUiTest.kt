package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareTermShareButtonUiTest {
	@Test
	fun when_thereIsAClosedTermAndItsBackground_then_theButtonIsEnabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermShareButton(
				term = closedTerm(),
				artwork = ShareBackground.included.toShareTermArtwork(),
				isSharing = false,
				onShareClick = {}
			)
		}

		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled()
	}

	@Test
	fun when_thereIsNoClosedTerm_then_theButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermShareButton(
				term = null,
				artwork = ShareBackground.included.toShareTermArtwork(),
				isSharing = false,
				onShareClick = {}
			)
		}

		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsNotEnabled()
	}

	private fun closedTerm() = ShareTermTermItem(
		key = "closed-2026",
		label = UiText.Raw("Ene - Mar 2026"),
		card = shareCardItem(),
		selected = true
	)
}
