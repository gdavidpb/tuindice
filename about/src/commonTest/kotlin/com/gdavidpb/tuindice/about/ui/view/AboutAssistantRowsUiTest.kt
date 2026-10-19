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
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AboutAssistantRowsUiTest {
	@Test
	fun when_assistantIsUnavailable_then_noAssistantRowIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAboutContent(
				state = About.State.Content(versionText = "1.2.3", assistantAvailable = false)
			)
		}

		assertNodeVisible(AboutUiTags.UsageDataConsentToggle)
		assertTrue(onAllNodesWithTag(AboutUiTags.AssistantConsentSwitch).fetchSemanticsNodes().isEmpty())
		assertTrue(onAllNodesWithTag(AboutUiTags.AssistantMemoryItem).fetchSemanticsNodes().isEmpty())
	}

	@Test
	fun when_assistantIsAvailable_then_switchReflectsTheStoredConsent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAboutContent(
				state = About.State.Content(
					versionText = "1.2.3",
					assistantAvailable = true,
					assistantConsentGranted = true
				)
			)
		}

		scrollToRow(AboutUiTags.AssistantConsentSwitch)
		onNodeWithTag(AboutUiTags.AssistantConsentSwitch).assertIsOn()
	}

	@Test
	fun when_assistantSwitchTapped_then_requestsTheToggledConsent() = runTuIndiceUiTest {
		val changes = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			AssistantAboutContent(
				state = About.State.Content(versionText = "1.2.3", assistantAvailable = true),
				onAssistantConsentChange = { granted -> changes += granted }
			)
		}

		scrollToRow(AboutUiTags.AssistantConsentSwitch)
		onNodeWithTag(AboutUiTags.AssistantConsentSwitch)
			.assertIsOff()
			.performClick()

		assertEquals(listOf(true), changes)
	}

	@Test
	fun when_assistantMemoryTapped_then_requestsTheMemory() = runTuIndiceUiTest {
		var memoryClicks = 0

		setTuIndiceTestContent {
			AssistantAboutContent(
				state = About.State.Content(versionText = "1.2.3", assistantAvailable = true),
				onAssistantMemoryClick = { memoryClicks++ }
			)
		}

		scrollToRow(AboutUiTags.AssistantMemoryItem)
		onNodeWithTag(AboutUiTags.AssistantMemoryItem).performClick()

		assertEquals(1, memoryClicks)
	}

	@Test
	fun when_assistantConversationsTapped_then_requestsTheConfirmation() = runTuIndiceUiTest {
		var conversationsClicks = 0

		setTuIndiceTestContent {
			AssistantAboutContent(
				state = About.State.Content(versionText = "1.2.3", assistantAvailable = true),
				onAssistantConversationsClick = { conversationsClicks++ }
			)
		}

		scrollToRow(AboutUiTags.AssistantConversationsItem)
		onNodeWithTag(AboutUiTags.AssistantConversationsItem).performClick()

		assertEquals(1, conversationsClicks)
	}

	@Test
	fun when_assistantIsUnavailable_then_noConversationsRowIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAboutContent(
				state = About.State.Content(versionText = "1.2.3", assistantAvailable = false)
			)
		}

		assertTrue(onAllNodesWithTag(AboutUiTags.AssistantConversationsItem).fetchSemanticsNodes().isEmpty())
	}

	@Test
	fun when_assistantIsAvailable_then_conversationsRowSitsBelowTheMemory() = runTuIndiceUiTest {
		// The rows alone, in a plain column: inside the lazy content the lower row can be out of the viewport, not laid out.
		setTuIndiceTestContent {
			Column {
				AboutAssistantItems(
					consentGranted = true,
					onConsentChange = {},
					onMemoryClick = {},
					onConversationsClick = {}
				)
			}
		}

		val memoryTop = onNodeWithTag(AboutUiTags.AssistantMemoryItem).fetchSemanticsNode().boundsInRoot.top
		val conversationsTop = onNodeWithTag(AboutUiTags.AssistantConversationsItem)
			.fetchSemanticsNode().boundsInRoot.top

		assertTrue(memoryTop < conversationsTop)
	}

	private fun ComposeUiTest.scrollToRow(tag: String) {
		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(tag))
	}

	@Composable
	private fun AssistantAboutContent(
		state: About.State.Content,
		onAssistantConsentChange: (granted: Boolean) -> Unit = {},
		onAssistantMemoryClick: () -> Unit = {},
		onAssistantConversationsClick: () -> Unit = {}
	) {
		AboutContentView(
			state = state,
			actions = AboutActions(
				onCreativeCommonsClick = {},
				onXClick = {},
				onGithubClick = {},
				onKotlinClick = {},
				onComposeClick = {},
				onFirebaseClick = {},
				onKoinClick = {},
				onKtorClick = {},
				onDstClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				onSupportClick = {},
				onShareAppClick = {},
				onRateOnPlayStoreClick = {},
				onContactDeveloperClick = {},
				onReportBugClick = {},
				onAssistantConsentChange = onAssistantConsentChange,
				onAssistantMemoryClick = onAssistantMemoryClick,
				onAssistantConversationsClick = onAssistantConversationsClick
			)
		)
	}
}
