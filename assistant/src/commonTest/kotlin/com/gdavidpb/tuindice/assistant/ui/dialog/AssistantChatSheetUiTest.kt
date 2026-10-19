package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.assistant.ui.contextChip
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantChatSheetUiTest {
	@Test
	fun when_conversationIsOpen_then_theSheetNeverNamesTheAssistant() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = contentState(items = listOf(agentMessage())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ChatSheet)
		onAllNodes(hasText("TuIndice", substring = true, ignoreCase = true), useUnmergedTree = true)
			.assertCountEquals(0)
		onAllNodes(hasContentDescription("TuIndice", substring = true, ignoreCase = true), useUnmergedTree = true)
			.assertCountEquals(0)
	}

	@Test
	fun when_conversationIsOpen_then_theTitleAsistenteSitsAboveTheContextChip_afterTheSparkle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = contentState(context = contextChip(), items = listOf(agentMessage())),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ChatTitle)
		onNodeWithTag(AssistantUiTags.ChatTitle).assertTextEquals(TITLE_TEXT)
		val title = onNodeWithTag(AssistantUiTags.ChatTitle).fetchSemanticsNode().boundsInRoot
		val sparkle = onNodeWithTag(AssistantUiTags.TitleSparkle, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val text = onNodeWithText(TITLE_TEXT, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val chip = onNodeWithTag(AssistantUiTags.ContextChip).fetchSemanticsNode().boundsInRoot

		assertTrue(title.bottom <= chip.top)
		assertEquals(chip.left, title.left)
		// The sparkle leads the title on its own line, centred on the text.
		assertEquals(title.left, sparkle.left)
		assertTrue(sparkle.right <= text.left)
		assertEquals(text.center.y, sparkle.center.y, absoluteTolerance = 1f)
	}

	@Test
	fun when_consentIsRequired_then_theConsentTitleIsTheOnlyTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = AssistantChat.State.ConsentRequired,
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.ConsentView)
		assertNodeHidden(AssistantUiTags.ChatTitle)
	}

	@Test
	fun when_consentIsRequired_then_theSheetAsksForAcceptance() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = AssistantChat.State.ConsentRequired,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.ConsentAccept)
		onNodeWithTag(AssistantUiTags.ConsentAccept).performClick()

		assertIs<AssistantChat.Action.AcceptConsent>(actions.single())
	}

	@Test
	fun when_theConversationArrivesWithAnimationsOff_then_theWholeSheetIsOnScreen_composerIncluded() = runTuIndiceUiTest {
		// The opening as the route lives it with consent given: the sheet opens on Idle while the status is read, and the
		// conversation arrives afterwards. With animations off nothing settles the sheet any further than it opens.
		val state = mutableStateOf<AssistantChat.State>(AssistantChat.State.Idle)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				AssistantChatSheet(state = state.value, onAction = {})
			}
		}

		waitForIdle()
		state.value = contentState(context = contextChip())
		waitForIdle()

		// The body takes 60% of the window: a sheet left half open would push the composer below the screen.
		onNodeWithTag(AssistantUiTags.Composer).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.SendButton).assertIsDisplayed()
	}

	@Test
	fun when_theScrimAboveTheSheetIsTapped_then_theChatIsDismissed() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatSheet(
				state = contentState(context = contextChip(), items = listOf(agentMessage())),
				onAction = { actions.add(it) }
			)
		}

		waitForIdle()
		// The body takes 60% of the window, so the top edge is always scrim.
		// The sheet draws in a layer of its own, so there are two roots; a touch on the window goes to the topmost layer.
		onAllNodes(isRoot())[0].performTouchInput { click(Offset(width / 2f, SCRIM_TAP_Y)) }
		waitForIdle()

		assertTrue(actions.any { it is AssistantChat.Action.Dismiss }, "actions were $actions")
	}

	@Test
	fun when_aNoticeIsShown_then_itSitsInsideTheSheetRightAboveTheComposer() = runTuIndiceUiTest {
		// The route shows the notices through the sheet's host; the test shows one the same way, from a composition.
		val hostState = SnackbarHostState()
		val notice = mutableStateOf<String?>(null)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				LaunchedEffect(notice.value) {
					notice.value?.let { message -> hostState.showSnackbar(message) }
				}
				AssistantChatSheet(
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

		// Both are in the sheet's own window, so their bounds compare: the notice sits right above the composer.
		val snackbar = onNodeWithTag(AssistantUiTags.ChatSnackbar).fetchSemanticsNode().boundsInRoot
		val composer = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		assertTrue(snackbar.bottom <= composer.top, "the notice is not above the composer: $snackbar over $composer")
	}

	private companion object {
		const val SCRIM_TAP_Y = 20f
		const val TITLE_TEXT = "Asistente"
		const val NOTICE_TEXT = "Gracias por tu opinión"
	}
}
