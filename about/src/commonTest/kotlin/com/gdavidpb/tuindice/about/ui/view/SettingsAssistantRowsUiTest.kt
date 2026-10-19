package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.testing.noSettingsActions
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SettingsAssistantRowsUiTest {
	@Test
	fun when_assistantIsUnavailable_then_noAssistantRowIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAboutContent(
				state = Settings.State.Content(identity = null, versionText = "1.2.3", assistantAvailable = false)
			)
		}

		assertNodeVisible(SettingsUiTags.UsageDataConsentToggle)
		assertTrue(onAllNodesWithTag(SettingsUiTags.AssistantConsentSwitch).fetchSemanticsNodes().isEmpty())
		assertTrue(onAllNodesWithTag(SettingsUiTags.AssistantMemoryItem).fetchSemanticsNodes().isEmpty())
	}

	@Test
	fun when_assistantIsAvailable_then_switchReflectsTheStoredConsent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAboutContent(
				state = Settings.State.Content(
					identity = null,
					versionText = "1.2.3",
					assistantAvailable = true,
					assistantConsentGranted = true
				)
			)
		}

		scrollToRow(SettingsUiTags.AssistantConsentSwitch)
		onNodeWithTag(SettingsUiTags.AssistantConsentSwitch).assertIsOn()
	}

	@Test
	fun when_assistantSwitchTapped_then_requestsTheToggledConsent() = runTuIndiceUiTest {
		val changes = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			AssistantAboutContent(
				state = Settings.State.Content(identity = null, versionText = "1.2.3", assistantAvailable = true),
				onAssistantConsentChange = { granted -> changes += granted }
			)
		}

		scrollToRow(SettingsUiTags.AssistantConsentSwitch)
		onNodeWithTag(SettingsUiTags.AssistantConsentSwitch)
			.assertIsOff()
			.performClick()

		assertEquals(listOf(true), changes)
	}

	@Test
	fun when_assistantMemoryTapped_then_requestsTheMemory() = runTuIndiceUiTest {
		var memoryClicks = 0

		setTuIndiceTestContent {
			AssistantAboutContent(
				state = Settings.State.Content(identity = null, versionText = "1.2.3", assistantAvailable = true),
				onAssistantMemoryClick = { memoryClicks++ }
			)
		}

		scrollToRow(SettingsUiTags.AssistantMemoryItem)
		onNodeWithTag(SettingsUiTags.AssistantMemoryItem).performClick()

		assertEquals(1, memoryClicks)
	}

	@Test
	fun when_assistantConversationsTapped_then_requestsTheConfirmation() = runTuIndiceUiTest {
		var conversationsClicks = 0

		setTuIndiceTestContent {
			AssistantAboutContent(
				state = Settings.State.Content(identity = null, versionText = "1.2.3", assistantAvailable = true),
				onAssistantConversationsClick = { conversationsClicks++ }
			)
		}

		scrollToRow(SettingsUiTags.AssistantConversationsItem)
		onNodeWithTag(SettingsUiTags.AssistantConversationsItem).performClick()

		assertEquals(1, conversationsClicks)
	}

	@Test
	fun when_assistantIsUnavailable_then_noConversationsRowIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAboutContent(
				state = Settings.State.Content(identity = null, versionText = "1.2.3", assistantAvailable = false)
			)
		}

		assertTrue(onAllNodesWithTag(SettingsUiTags.AssistantConversationsItem).fetchSemanticsNodes().isEmpty())
	}

	@Test
	fun when_assistantIsAvailable_then_conversationsRowSitsBelowTheMemory() = runTuIndiceUiTest {
		// The rows alone, in a plain column: inside the lazy content the lower row can be out of the viewport, not laid out.
		setTuIndiceTestContent {
			Column {
				SettingsAssistantItems(
					consentGranted = true,
					onConsentChange = {},
					onMemoryClick = {},
					onConversationsClick = {}
				)
			}
		}

		val memoryTop = onNodeWithTag(SettingsUiTags.AssistantMemoryItem).fetchSemanticsNode().boundsInRoot.top
		val conversationsTop = onNodeWithTag(SettingsUiTags.AssistantConversationsItem)
			.fetchSemanticsNode().boundsInRoot.top

		assertTrue(memoryTop < conversationsTop)
	}

	private fun ComposeUiTest.scrollToRow(tag: String) {
		onNodeWithTag(SettingsUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(tag))
	}

	@Composable
	private fun AssistantAboutContent(
		state: Settings.State.Content,
		onAssistantConsentChange: (granted: Boolean) -> Unit = {},
		onAssistantMemoryClick: () -> Unit = {},
		onAssistantConversationsClick: () -> Unit = {}
	) {
		SettingsContentView(
			state = state,
			actions = noSettingsActions().copy(
				onAssistantConsentChange = onAssistantConsentChange,
				onAssistantMemoryClick = onAssistantMemoryClick,
				onAssistantConversationsClick = onAssistantConversationsClick
			),
			profilePhoto = {}
		)
	}
}
