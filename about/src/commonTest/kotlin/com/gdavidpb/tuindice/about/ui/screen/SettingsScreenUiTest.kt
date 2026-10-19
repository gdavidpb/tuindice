package com.gdavidpb.tuindice.about.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.testing.noSettingsActions
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SettingsScreenUiTest {
	@Test
	fun when_stateIsContent_then_rendersContentView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsScreen(
				state = Settings.State.Content(
					identity = null,
					versionText = "1.2.3",
					usageDataCollectionEnabled = false,
					assistantAvailable = false,
					assistantConsentGranted = false
				),
				actions = noSettingsActions(),
				profilePhoto = {}
			)
		}

		assertNodeVisible(SettingsUiTags.ContentContainer)
		assertNodeVisible(SettingsUiTags.OpenPrivacy)
	}

	@Test
	fun when_stateIsIdle_then_hidesContentItems() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsScreen(
				state = Settings.State.Idle,
				actions = noSettingsActions(),
				profilePhoto = {}
			)
		}

		assertNodeHidden(SettingsUiTags.ContentContainer)
		assertNodeHidden(SettingsUiTags.OpenPrivacy)
		assertNodeHidden(SettingsUiTags.SignOut)
	}

	@Test
	fun when_stateIsContentAndItemsTapped_then_propagatesAllCallbacks() = runTuIndiceUiTest {
		val clicks = mutableListOf<String>()

		setTuIndiceTestContent {
			SettingsScreen(
				state = Settings.State.Content(
					identity = null,
					versionText = "1.2.3",
					usageDataCollectionEnabled = false,
					assistantAvailable = true,
					assistantConsentGranted = false
				),
				actions = noSettingsActions().copy(
					onXClick = { clicks += "x" },
					onTermsAndConditionsClick = { clicks += "terms" },
					onPrivacyPolicyClick = { clicks += "privacy" },
					onSupportClick = { clicks += "support" },
					onShareAppClick = { clicks += "share" },
					onRateOnPlayStoreClick = { clicks += "rate" },
					onReportBugClick = { clicks += "report" },
					onAboutClick = { clicks += "about" },
					onSignOutClick = { clicks += "sign-out" },
					onAssistantMemoryClick = { clicks += "memory" },
					onAssistantConversationsClick = { clicks += "conversations" }
				),
				profilePhoto = {}
			)
		}

		val tapped = listOf(
			SettingsUiTags.AssistantMemoryItem to "memory",
			SettingsUiTags.AssistantConversationsItem to "conversations",
			SettingsUiTags.OpenPrivacy to "privacy",
			SettingsUiTags.OpenTerms to "terms",
			SettingsUiTags.OpenSupport to "support",
			SettingsUiTags.ReportBug to "report",
			SettingsUiTags.ShareApp to "share",
			SettingsUiTags.RateOnStore to "rate",
			SettingsUiTags.OpenX to "x",
			SettingsUiTags.OpenAbout to "about",
			SettingsUiTags.SignOut to "sign-out"
		)

		tapped.forEach { (tag, _) ->
			onNodeWithTag(SettingsUiTags.ContentContainer).performScrollToNode(hasTestTag(tag))
			onNodeWithTag(tag).performClick()
		}

		assertEquals(tapped.map { (_, name) -> name }, clicks)
	}
}
