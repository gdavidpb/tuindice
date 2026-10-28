package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

// The list's follow, on the test clock. It moves to its last item when a new one arrives and when the keyboard opens or
// closes; a change inside its last item leaves the list where the person has put it.
@OptIn(ExperimentalTestApi::class)
class AssistantChatConversationViewUiTest {
	@Test
	fun when_theLastMessageChangesInside_then_theListStaysWhereItIs() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = content.value,
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()
		scrollToTop()
		onNodeWithText(FIRST_TEXT).assertIsDisplayed()

		content.value = conversation(lastUseful = true)
		waitForIdle()

		onNodeWithText(FIRST_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_theKeyboardOpensOrCloses_then_theListFollowsItsLastMessage() = runTuIndiceUiTest {
		val keyboard = FakeKeyboardInsets()
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalChatKeyboardInsets provides keyboard) {
				AssistantChatConversationView(
					content = conversation(lastUseful = null),
					onAction = {},
					modifier = Modifier.height(CONTAINER_HEIGHT)
				)
			}
		}
		waitForIdle()
		scrollToTop()

		keyboard.bottomPx = KEYBOARD_PX
		waitForIdle()
		onNodeWithText(LAST_TEXT).assertIsDisplayed()

		scrollToTop()
		keyboard.bottomPx = 0
		waitForIdle()
		onNodeWithText(LAST_TEXT).assertIsDisplayed()
	}

	// A short window holding the notice and the suggestions of an empty chat, with the keyboard open. The composer
	// still ends above the keyboard: the part above it scrolls, so it never pushes the composer out of the window.
	@Test
	fun when_noticeAndSuggestionsDoNotFitWithTheKeyboardOpen_then_theComposerStaysAboveIt() = runTuIndiceUiTest {
		setShortChatWithKeyboardOpen()

		// Fetching the node fails the test when the notice is not on the screen, which is the existence check.
		onNodeWithText(OFFER_TITLE).fetchSemanticsNode()
		val composer = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		// The field keeps its full height: a composer squeezed to nothing at the keyboard's edge is not usable either.
		assertTrue(
			composer.height >= COMPOSER_MIN_HEIGHT_PX,
			"The composer is ${composer.height} tall, below the height of its field."
		)
		assertTrue(composer.top >= 0f, "The composer starts at ${composer.top}, above the window.")
		assertTrue(
			composer.bottom <= KEYBOARD_TOP_PX,
			"The composer ends at ${composer.bottom}, under the keyboard that starts at $KEYBOARD_TOP_PX."
		)
	}

	// The part above the composer scrolls when it does not fit, so the last suggestion can be brought up
	// above the keyboard.
	@Test
	fun when_theLastSuggestionIsBelowTheFold_then_scrollingBringsItAboveTheKeyboard() = runTuIndiceUiTest {
		setShortChatWithKeyboardOpen()

		onNodeWithText(LAST_SUGGESTION).performScrollTo()
		waitForIdle()

		val suggestion = onNodeWithText(LAST_SUGGESTION).fetchSemanticsNode().boundsInRoot
		assertTrue(
			suggestion.bottom <= KEYBOARD_TOP_PX,
			"The last suggestion ends at ${suggestion.bottom}, under the keyboard that starts at $KEYBOARD_TOP_PX."
		)
	}

	private fun ComposeUiTest.setShortChatWithKeyboardOpen() {
		val keyboard = FakeKeyboardInsets().apply { bottomPx = SHORT_KEYBOARD_PX }

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalChatKeyboardInsets provides keyboard) {
				AssistantChatConversationView(
					content = contentState(suggestions = SUGGESTIONS).copy(notificationOffer = true),
					onAction = {},
					modifier = Modifier.height(SHORT_CONTAINER_DP.dp)
				)
			}
		}
		waitForIdle()
	}

	private fun ComposeUiTest.scrollToTop() {
		onNodeWithTag(AssistantUiTags.MessageList).performScrollToIndex(0)
		waitForIdle()
	}

	private fun conversation(lastUseful: Boolean?): AssistantChat.State.Content =
		contentState(
			items = (1..MESSAGE_COUNT).map { number ->
				agentMessage(
					id = "message-$number",
					text = "Respuesta $number",
					useful = if (number == MESSAGE_COUNT) lastUseful else null
				)
			}
		)

	private companion object {
		val CONTAINER_HEIGHT = 480.dp
		const val MESSAGE_COUNT = 12
		const val FIRST_TEXT = "Respuesta 1"
		const val LAST_TEXT = "Respuesta 12"
		const val KEYBOARD_PX = 160

		// A window too short for the notice, the suggestions and the composer at once, with the keyboard open beside them.
		const val SHORT_CONTAINER_DP = 420
		const val SHORT_KEYBOARD_PX = 200
		const val KEYBOARD_TOP_PX = SHORT_CONTAINER_DP - SHORT_KEYBOARD_PX

		// The composer's field is at least 56 dp tall at the density of these tests, which is 1 px per dp.
		const val COMPOSER_MIN_HEIGHT_PX = 56
		const val OFFER_TITLE = "Avisos de tu informe"
		const val LAST_SUGGESTION = "¿Cuál es mi ritmo de créditos?"
		val SUGGESTIONS = listOf(
			ChatSuggestionItem(id = "trimestre", label = "¿Cómo voy este trimestre?"),
			ChatSuggestionItem(id = "atencion", label = "¿Qué necesita mi atención?"),
			ChatSuggestionItem(id = "creditos", label = LAST_SUGGESTION)
		)
	}
}

// A keyboard whose height the test sets: the conversation follows its insets as it follows the real keyboard's.
private class FakeKeyboardInsets : WindowInsets {
	var bottomPx by mutableStateOf(0)

	override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = 0

	override fun getTop(density: Density): Int = 0

	override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = 0

	override fun getBottom(density: Density): Int = bottomPx
}
