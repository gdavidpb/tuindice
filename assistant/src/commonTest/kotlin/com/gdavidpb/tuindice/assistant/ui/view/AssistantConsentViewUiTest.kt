package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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

	@Test
	fun when_consentIsShown_then_theAccuracyWarningSitsBetweenScopeAndProviders() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantConsentView(onAction = {})
		}

		onNodeWithText(ACCURACY_TEXT).assertIsDisplayed()
		val scopeTop = onNodeWithText(SCOPE_TEXT).fetchSemanticsNode().boundsInRoot.top
		val accuracyTop = onNodeWithText(ACCURACY_TEXT).fetchSemanticsNode().boundsInRoot.top
		val providersTop = onNodeWithText(PROVIDERS_TEXT).fetchSemanticsNode().boundsInRoot.top

		assertTrue(scopeTop < accuracyTop && accuracyTop < providersTop)
	}

	private companion object {
		const val SCOPE_TEXT =
			"El asistente responde tus preguntas sobre tu historial académico. " +
			"Solo propone cambios y los hace si tú los confirmas."
		const val ACCURACY_TEXT = "Puede equivocarse: revisa las cifras importantes en la app antes de decidir."
		const val PROVIDERS_TEXT = "Tus mensajes se procesan con proveedores externos: Anthropic y Google."
	}
}
