package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
	fun when_theAcceptanceFailed_then_theErrorIsShown_andAcceptStillDispatchesAccept() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantConsentView(onAction = { actions.add(it) }, failed = true)
		}

		assertNodeVisible(AssistantUiTags.ConsentFailed)
		onNodeWithTag(AssistantUiTags.ConsentAccept).performClick()

		assertIs<AssistantChat.Action.AcceptConsent>(actions.single())
	}

	@Test
	fun when_privacyLinkIsTapped_then_opensThePrivacyPolicy() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantConsentView(onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.ConsentPrivacy)
		onNodeWithTag(AssistantUiTags.ConsentPrivacy).performClick()

		assertIs<AssistantChat.Action.OpenPrivacyPolicy>(actions.single())
	}

	@Test
	fun when_consentIsShown_then_thePrivacyLinkStartsAtTheRowsMargin_withATouchAreaOf48Dp() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantConsentView(onAction = {})
		}

		val link = onNodeWithTag(AssistantUiTags.ConsentPrivacy)
		link.assertHeightIsAtLeast(48.dp)
		val linkLeft = link.fetchSemanticsNode().boundsInRoot.left
		val titleLeft = onNodeWithTag(AssistantUiTags.ConsentTitle).fetchSemanticsNode().boundsInRoot.left
		val marginLeft = onNodeWithTag(AssistantUiTags.ConsentView).fetchSemanticsNode().boundsInRoot.left

		assertEquals(titleLeft, linkLeft)
		assertEquals(marginLeft, linkLeft)
	}

	@Test
	fun when_consentIsShown_then_theNegativeButtonSitsBeforeAccept() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantConsentView(onAction = {})
		}

		onNodeWithText(DECLINE_TEXT).assertIsDisplayed()
		val declineRight = onNodeWithTag(AssistantUiTags.ConsentDecline).fetchSemanticsNode().boundsInRoot.right
		val acceptLeft = onNodeWithTag(AssistantUiTags.ConsentAccept).fetchSemanticsNode().boundsInRoot.left

		assertTrue(declineRight <= acceptLeft)
	}

	@Test
	fun when_consentIsShown_then_theTitleAndTheFiveRowsAreInOrder_andNoTwoInARowOpenWithTheSameWords() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				AssistantConsentView(onAction = {})
			}

			onNodeWithText(TITLE_TEXT).assertIsDisplayed()
			val tops = (listOf(TITLE_TEXT) + LINES).map { text ->
				onNodeWithText(text).fetchSemanticsNode().boundsInRoot.top
			}

			assertEquals(tops.sorted(), tops)
			LINES.zipWithNext().forEach { (previous, next) ->
				assertFalse(previous.startsWith(MESSAGES_OPENING) && next.startsWith(MESSAGES_OPENING))
			}
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

	@Test
	fun when_consentIsShown_then_theSparkleLeadsTheTitleOnItsLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantConsentView(onAction = {})
		}

		onNodeWithTag(AssistantUiTags.ConsentTitle).assertTextEquals(TITLE_TEXT)
		val sparkle = onNodeWithTag(AssistantUiTags.TitleSparkle, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val text = onNodeWithText(TITLE_TEXT, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(sparkle.right <= text.left)
		assertEquals(text.center.y, sparkle.center.y, absoluteTolerance = 1f)
	}

	private companion object {
		const val TITLE_TEXT = "Antes de usar el asistente"
		const val DECLINE_TEXT = "Ahora no"
		const val MESSAGES_OPENING = "Tus mensajes"
		const val SCOPE_TEXT =
			"Responde tus preguntas sobre tu historial académico y solo propone cambios: " +
				"los hace si tú los confirmas."
		const val ACCURACY_TEXT = "Puede equivocarse: revisa las cifras importantes en la app antes de decidir."
		const val PROVIDERS_TEXT = "Tus mensajes se procesan con proveedores externos: Anthropic y Google."
		const val RETENTION_TEXT = "Guardamos tus mensajes durante 12 meses."
		const val CONTROL_TEXT = "Puedes apagar el asistente y eliminar tus datos desde Ajustes, " +
			"tocando tu foto arriba a la derecha."
		val LINES = listOf(SCOPE_TEXT, ACCURACY_TEXT, PROVIDERS_TEXT, RETENTION_TEXT, CONTROL_TEXT)
	}
}
