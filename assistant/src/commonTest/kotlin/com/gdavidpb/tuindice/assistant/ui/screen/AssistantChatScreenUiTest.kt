package com.gdavidpb.tuindice.assistant.ui.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatScreenUiTest {
	@Test
	fun when_theScreenIsLoading_then_itsHeaderAndBackButtonAreAvailable() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatScreen(state = AssistantChat.State.Idle, onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.ChatTitle)
		onNodeWithTag(AssistantUiTags.ChatBack).performClick()
		assertIs<AssistantChat.Action.Dismiss>(actions.single())
	}

	@Test
	fun when_conversationIsOpen_then_theScreenDoesNotNameTheApp() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatScreen(
				state = contentState(items = listOf(agentMessage())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ChatScreen)
		onAllNodes(hasText("TuIndice", substring = true, ignoreCase = true), useUnmergedTree = true)
			.assertCountEquals(0)
		onAllNodes(hasContentDescription("TuIndice", substring = true, ignoreCase = true), useUnmergedTree = true)
			.assertCountEquals(0)
	}

	@Test
	fun when_conversationIsOpen_then_theTitleSitsAboveTheMessages_afterTheSparkle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatScreen(
				state = contentState(items = listOf(agentMessage())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ChatTitle)
		onNodeWithTag(AssistantUiTags.ChatTitle).assertTextEquals(TITLE_TEXT)
		val title = onNodeWithTag(AssistantUiTags.ChatTitle).fetchSemanticsNode().boundsInRoot
		val sparkle = onNodeWithTag(AssistantUiTags.TitleSparkle, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val text = onNodeWithText(TITLE_TEXT, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val messages = onNodeWithTag(AssistantUiTags.MessageList).fetchSemanticsNode().boundsInRoot
		val back = onNodeWithTag(AssistantUiTags.ChatBack).fetchSemanticsNode().boundsInRoot

		assertTrue(title.bottom <= messages.top)
		assertTrue(back.right <= title.left)
		// The sparkle leads the title on its own line, centred on the text.
		assertEquals(title.left, sparkle.left)
		assertTrue(sparkle.right <= text.left)
		assertEquals(text.center.y, sparkle.center.y, absoluteTolerance = 1f)
	}

	@Test
	fun when_consentIsRequired_then_theScreenHeaderAndConsentTitleAreVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatScreen(
				state = AssistantChat.State.ConsentRequired,
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ConsentView)
		assertNodeVisible(AssistantUiTags.ChatTitle)
		assertNodeVisible(AssistantUiTags.ConsentTitle)
	}

	@Test
	fun when_consentIsRequired_then_theScreenAsksForAcceptance() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatScreen(
				state = AssistantChat.State.ConsentRequired,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.ConsentAccept)
		onNodeWithTag(AssistantUiTags.ConsentAccept).performClick()

		assertIs<AssistantChat.Action.AcceptConsent>(actions.single())
	}

	@Test
	fun when_theConversationArrivesWithAnimationsOff_then_theComposerIsOnScreen() = runTuIndiceUiTest {
		// The route opens on Idle while status is read; the conversation arrives afterwards.
		val state = mutableStateOf<AssistantChat.State>(AssistantChat.State.Idle)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				AssistantChatScreen(state = state.value, onAction = {})
			}
		}

		waitForIdle()
		state.value = contentState()
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Composer).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.SendButton).assertIsDisplayed()
	}

	@Test
	fun when_backIsTapped_then_theChatIsDismissed() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatScreen(
				state = contentState(items = listOf(agentMessage())),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithTag(AssistantUiTags.ChatBack).performClick()
		waitForIdle()

		assertTrue(actions.any { it is AssistantChat.Action.Dismiss }, "actions were $actions")
	}

	@Test
	fun when_aNoticeIsShown_then_itSitsInsideTheScreenRightAboveTheComposer() = runTuIndiceUiTest {
		// The route shows the notices through the screen's host; the test shows one the same way.
		val hostState = SnackbarHostState()
		val notice = mutableStateOf<String?>(null)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				LaunchedEffect(notice.value) {
					notice.value?.let { message -> hostState.showSnackbar(message) }
				}
				AssistantChatScreen(
					state = contentState(items = listOf(agentMessage())),
					onAction = {},
					snackbarHostState = hostState
				)
			}
		}

		waitForIdle()
		notice.value = NOTICE_TEXT
		waitForIdle()

		assertNodeVisible(AssistantUiTags.ChatSnackbar)
		onNodeWithTag(AssistantUiTags.ChatSnackbarMessage, useUnmergedTree = true)
			.assertTextEquals(NOTICE_TEXT)
			.assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Composer).assertIsDisplayed()

		// The notice sits right above the composer.
		val snackbar = onNodeWithTag(AssistantUiTags.ChatSnackbar).fetchSemanticsNode().boundsInRoot
		val composer = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		assertTrue(snackbar.bottom <= composer.top, "the notice is not above the composer: $snackbar over $composer")
	}

	private companion object {
		const val TITLE_TEXT = "Asistente"
		const val NOTICE_TEXT = "Gracias por tu opinión"
	}
}
