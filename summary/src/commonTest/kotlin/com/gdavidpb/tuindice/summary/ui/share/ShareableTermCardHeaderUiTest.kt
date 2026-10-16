package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareableTermCardHeaderUiTest {
	@Test
	fun when_itIsShown_then_itShowsItsTitleAndItsTerm() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box { ShareableTermCardHeader(term = shareCardItem()) }
		}

		onNodeWithText("Mi trimestre").assertIsDisplayed()
		onNodeWithText("Enero - Marzo 2026").assertIsDisplayed()
	}

	@Test
	fun when_itIsShown_then_theUniversityLogoSignsIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box { ShareableTermCardHeader(term = shareCardItem()) }
		}

		onNodeWithTag(SummaryUiTags.ShareTermCardLogo, useUnmergedTree = true).assertIsDisplayed()
	}
}
