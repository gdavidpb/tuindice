package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantConsentViewUiTest {
	@Test
	fun when_consentIsShown_then_acceptAndDeclineDispatchTheirActions() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantConsentView(onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.ConsentAccept)
		onNodeWithTag(AssistantUiTags.ConsentDecline).performClick()
		onNodeWithTag(AssistantUiTags.ConsentAccept).performClick()

		assertIs<AssistantChat.Action.DeclineConsent>(actions[0])
		assertIs<AssistantChat.Action.AcceptConsent>(actions[1])
	}

	@Test
	fun when_privacyLinkIsTapped_then_opensTheAboutScreen() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantConsentView(onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.ConsentPrivacy)
		onNodeWithTag(AssistantUiTags.ConsentPrivacy).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.ABOUT, open.target)
		assertTrue(open.entityRefs.isEmpty())
	}
}
