package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareTermSheetContentUiTest {
	@Test
	fun when_thereIsAClosedTermAndABackground_then_thePreviewAndTheChipsAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermSheetContent(content = content(withTerm = true), actions = actions())
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermTerms).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermPreview).assertCountEquals(1)
	}

	@Test
	fun when_noTermHasClosed_then_theSheetSaysSoAndShowsNoPreview() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermSheetContent(content = content(withTerm = false), actions = actions())
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermEmpty).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermPreview).assertCountEquals(0)
	}

	private fun content(withTerm: Boolean) = ShareTerm.State.Content(
		terms = if (withTerm) {
			listOf(
				ShareTermTermItem(
					key = "closed-2026",
					label = UiText.Raw("Ene - Mar 2026"),
					card = shareCardItem(),
					selected = true
				)
			)
		} else {
			emptyList()
		},
		backgrounds = listOf(
			ShareTermBackgroundItem(
				artwork = ShareBackground.included.toShareTermArtwork(),
				selected = true
			)
		)
	)

	private fun actions() = ShareTermSheetActions(
		onTermClick = {},
		onBackgroundClick = {},
		onShareClick = {}
	)
}
