package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.testing.noAboutActions
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AboutContentViewUiTest {
	@Test
	fun when_stateContainsVersion_then_displaysVersionDescription() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutContentView(
				state = About.State.Content(versionText = "9.9.9"),
				actions = noAboutActions()
			)
		}

		onNodeWithText("9.9.9", substring = true).assertIsDisplayed()
	}

	@Test
	fun when_contentIsRendered_then_keepsOnlyWhatRemainsOfTheOldAbout() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutContentView(
				state = About.State.Content(versionText = "1.2.3"),
				actions = noAboutActions()
			)
		}

		// The license, the developer and their contact come first; the privacy, support, share and sign-out rows moved
		// to Settings, so none of them is here.
		assertNodeVisible(AboutUiTags.OpenCreativeCommons)
		assertNodeVisible(AboutUiTags.OpenGithub)
		assertNodeVisible(AboutUiTags.ContactDeveloper)
		listOf(
			SettingsUiTags.OpenTerms,
			SettingsUiTags.OpenPrivacy,
			SettingsUiTags.OpenSupport,
			SettingsUiTags.ReportBug,
			SettingsUiTags.ShareApp,
			SettingsUiTags.RateOnStore,
			SettingsUiTags.OpenX,
			SettingsUiTags.UsageDataConsentToggle,
			SettingsUiTags.SignOut
		).forEach { movedTag ->
			assertEquals(0, onAllNodesWithTag(movedTag).fetchSemanticsNodes().size, movedTag)
		}
	}
}
