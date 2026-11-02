package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.assistant.ui.screen.AssistantChatScreen
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

// The list's follow, on the test clock. It moves to its last item when a new one arrives and when the keyboard opens or
// closes; a change inside its last item leaves the list where the person has put it.
@OptIn(ExperimentalTestApi::class)
class AssistantChatConversationViewUiTest {
	@Test
	fun when_theScreenHasContext_then_theConversationStartsBelowTheTitleWithoutAContextCard() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatScreen(
				state = contentState(items = listOf(agentMessage())),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}

		val title = onNodeWithTag(AssistantUiTags.ChatTitle).fetchSemanticsNode().boundsInRoot
		val messages = onNodeWithTag(AssistantUiTags.MessageList).fetchSemanticsNode().boundsInRoot
		assertTrue(messages.top - title.bottom in 0f..with(density) { 40.dp.toPx() })
	}

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

		// The offer is on the screen, not only in the tree: a region scrolled past it leaves its title zero tall.
		val offer = onNodeWithText(OFFER_TITLE).fetchSemanticsNode().boundsInRoot
		assertTrue(offer.height > 0f, "The offer's title is out of the window, at $offer.")
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

	// A failed turn in a short window with the keyboard open: what the region holds does not fit, and its foot, the
	// notice with its retry, is shown whole right above the composer, where a tap on the retry retries the turn.
	@Test
	fun when_aTurnFailsWithTheKeyboardOpen_then_itsRetryShowsWholeAboveTheComposer() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		val keyboard = FakeKeyboardInsets().apply { bottomPx = SHORT_KEYBOARD_PX }
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalChatKeyboardInsets provides keyboard) {
				AssistantChatConversationView(
					content = contentState(
						items = listOf(ChatItem.User(id = "question", text = "Falla una vez", isFailed = true))
					).copy(notice = ChatNoticeItem.TurnFailed(message = FAILED_MESSAGE, canRetry = true)),
					onAction = { actions.add(it) },
					modifier = Modifier.height(SHORT_CONTAINER_DP.dp)
				)
			}
		}
		waitForIdle()

		val retry = onNodeWithTag(AssistantUiTags.NoticeRetry).fetchSemanticsNode().boundsInRoot
		val composer = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot
		assertTrue(
			retry.height >= RETRY_MIN_HEIGHT_PX,
			"The retry is ${retry.height} tall: the region cuts it, below its button's height."
		)
		assertTrue(retry.top >= 0f, "The retry starts at ${retry.top}, above the window.")
		assertTrue(
			retry.bottom <= composer.top,
			"The retry ends at ${retry.bottom}, under the composer that starts at ${composer.top}."
		)
		assertTrue(
			composer.bottom <= KEYBOARD_TOP_PX,
			"The composer ends at ${composer.bottom}, under the keyboard that starts at $KEYBOARD_TOP_PX."
		)

		onNodeWithTag(AssistantUiTags.NoticeRetry).performClick()

		assertIs<AssistantChat.Action.RetryTurn>(actions.single())
	}

	@Test
	fun when_theStudentWritesTheirOwnQuestion_then_theSuggestionsLeaveAndComeBackWhenTheTextIsCleared() =
		runTuIndiceUiTest {
			val content = mutableStateOf(contentState(suggestions = SUGGESTIONS))
			setTuIndiceTestContent {
				AssistantChatConversationView(
					content = content.value,
					onAction = {},
					modifier = Modifier.height(CONTAINER_HEIGHT)
				)
			}
			waitForIdle()
			onNodeWithText(LAST_SUGGESTION).assertIsDisplayed()

			content.value = content.value.copy(composer = content.value.composer.copy(text = TYPED_TEXT))
			waitForIdle()
			onNodeWithText(LAST_SUGGESTION).assertDoesNotExist()

			content.value = content.value.copy(composer = content.value.composer.copy(text = ""))
			waitForIdle()
			onNodeWithText(LAST_SUGGESTION).assertIsDisplayed()
		}

	@Test
	fun when_guidedModeKeepsARejectedDraft_then_theSuggestionsRemainAvailable() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = contentState(
					composer = ChatComposerItem(text = TYPED_TEXT, isEnabled = false, isSendEnabled = false),
					suggestions = SUGGESTIONS,
					isGuided = true
				).copy(notice = ChatNoticeItem.Guided(message = GUIDED_NOTICE)),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()

		onNodeWithText(LAST_SUGGESTION).assertIsDisplayed()
	}

	@Test
	fun when_theListIsAwayFromItsEnd_then_theScrollToEndButtonShows_andTakesItBackToTheEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = conversation(lastUseful = null),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()

		// The list follows its last message, so it opens at its end and the button is not there.
		onNodeWithTag(AssistantUiTags.ScrollToEndButton).assertDoesNotExist()

		scrollToTop()
		onNodeWithTag(AssistantUiTags.ScrollToEndButton).assertIsDisplayed()

		onNodeWithTag(AssistantUiTags.ScrollToEndButton).performClick()
		waitForIdle()

		onNodeWithText(LAST_TEXT).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.ScrollToEndButton).assertDoesNotExist()
	}

	// run_started clears the sending before the turn's first step has a label: the wait still says what is going on.
	@Test
	fun when_aTurnRunsWithNoStepLabelYet_then_theWaitingLabelShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = contentState(
					composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isStoppable = true)
				).copy(progressLabel = WAITING_PROGRESS),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()

		onNodeWithTag(AssistantUiTags.ProgressLabel, useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_aStudentWaitsForAnAnswer_then_progressFollowsTheirMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = waitingConversation(FIRST_PROGRESS),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()

		val question = onNodeWithTag(AssistantUiTags.Message.bubble(QUESTION_ID)).fetchSemanticsNode().boundsInRoot
		val progress = onNodeWithTag(AssistantUiTags.ProgressContainer).fetchSemanticsNode().boundsInRoot
		val composer = onNodeWithTag(AssistantUiTags.Composer).fetchSemanticsNode().boundsInRoot

		assertTrue(progress.top >= question.bottom, "The progress appears above the student's message.")
		assertTrue(
			progress.top - question.bottom <= with(density) { 24.dp.toPx() },
			"The progress is detached from the message."
		)
		assertTrue(
			composer.top - progress.bottom >= with(density) { 40.dp.toPx() },
			"The progress is pinned to the composer."
		)
	}

	@Test
	fun when_progressChanges_then_theLabelUpdatesBelowTheSameMessage() = runTuIndiceUiTest {
		val content = mutableStateOf(waitingConversation(FIRST_PROGRESS))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		onNodeWithText(FIRST_PROGRESS, useUnmergedTree = true).assertIsDisplayed()

		content.value = content.value.copy(progressLabel = SECOND_PROGRESS)
		waitForIdle()

		onNodeWithText(FIRST_PROGRESS, useUnmergedTree = true).assertDoesNotExist()
		onNodeWithText(SECOND_PROGRESS, useUnmergedTree = true).assertIsDisplayed()
		val question = onNodeWithTag(AssistantUiTags.Message.bubble(QUESTION_ID)).fetchSemanticsNode().boundsInRoot
		val progress = onNodeWithTag(AssistantUiTags.ProgressContainer).fetchSemanticsNode().boundsInRoot
		assertTrue(progress.top - question.bottom in 0f..with(density) { 24.dp.toPx() })
	}

	@Test
	fun when_theAnswerArrivesBeforeTheTurnEnds_then_progressStaysBelowTheStudent() = runTuIndiceUiTest {
		val waiting = waitingConversation(FIRST_PROGRESS)
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = waiting.copy(items = waiting.items + agentMessage(id = "reply", text = "Respuesta")),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()

		val question = onNodeWithTag(AssistantUiTags.Message.bubble(QUESTION_ID)).fetchSemanticsNode().boundsInRoot
		val progress = onNodeWithTag(AssistantUiTags.ProgressContainer).fetchSemanticsNode().boundsInRoot
		val reply = onNodeWithTag(AssistantUiTags.Message.agent("reply")).fetchSemanticsNode().boundsInRoot
		assertTrue(progress.top >= question.bottom, "The progress appears above the student's message.")
		assertTrue(progress.bottom <= reply.top, "The progress moved below the assistant's answer.")
	}

	@Test
	fun when_aPhotoIsBeingRead_then_progressFollowsTheLatestMessage() = runTuIndiceUiTest {
		val waiting = waitingConversation(FIRST_PROGRESS)
		setTuIndiceTestContent {
			AssistantChatConversationView(
				content = waiting.copy(
					items = waiting.items + agentMessage(id = "reply", text = "Respuesta"),
					progressLabel = PHOTO_PROGRESS,
					progressAnchorId = "reply",
					composer = waiting.composer.copy(isStoppable = false)
				),
				onAction = {},
				modifier = Modifier.height(CONTAINER_HEIGHT)
			)
		}
		waitForIdle()

		val reply = onNodeWithTag(AssistantUiTags.Message.agent("reply")).fetchSemanticsNode().boundsInRoot
		val progress = onNodeWithTag(AssistantUiTags.ProgressContainer).fetchSemanticsNode().boundsInRoot
		assertTrue(progress.top >= reply.bottom, "The photo status appears before the latest message.")
	}

	@Test
	fun when_aNewQuestionStartsWaiting_then_itsProgressIsVisibleAtTheEnd() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()

		content.value = content.value.copy(
			items = content.value.items + ChatItem.User(id = QUESTION_ID, text = QUESTION, isFailed = false),
			progressLabel = FIRST_PROGRESS,
			progressAnchorId = QUESTION_ID,
			composer = content.value.composer.copy(isStoppable = true)
		)
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Message.bubble(QUESTION_ID)).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.ProgressContainer).assertIsDisplayed()
		assertProgressShowsWhole()
	}

	@Test
	fun when_progressChangesWhileReadingHistory_then_theListStaysWhereItIs() = runTuIndiceUiTest {
		val history = conversation(lastUseful = null)
		val content = mutableStateOf(
			history.copy(
				items = history.items + ChatItem.User(id = QUESTION_ID, text = QUESTION, isFailed = false),
				progressLabel = FIRST_PROGRESS,
				progressAnchorId = QUESTION_ID,
				composer = history.composer.copy(isStoppable = true)
			)
		)
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollToTop()
		onNodeWithText(FIRST_TEXT).assertIsDisplayed()

		content.value = content.value.copy(progressLabel = SECOND_PROGRESS)
		waitForIdle()

		onNodeWithText(FIRST_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_progressAppearsWhileReadingHistory_then_theListStaysWhereItIs() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollToTop()
		onNodeWithText(FIRST_TEXT).assertIsDisplayed()

		content.value = content.value.copy(progressLabel = FIRST_PROGRESS, progressAnchorId = LAST_MESSAGE_ID)
		waitForIdle()

		onNodeWithText(FIRST_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_progressAppearsAtTheEnd_then_itShowsWhole() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()

		content.value = content.value.copy(progressLabel = FIRST_PROGRESS, progressAnchorId = LAST_MESSAGE_ID)
		waitForIdle()

		assertProgressShowsWhole()
	}

	@Test
	fun when_theAnswerArrivesWhileReadingHistory_then_theListStaysWhereItIs() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollToTop()
		onNodeWithText(FIRST_TEXT).assertIsDisplayed()

		content.value = content.value.copy(items = content.value.items + agentMessage(id = "reply", text = "Respuesta"))
		waitForIdle()

		onNodeWithText(FIRST_TEXT).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.ScrollToEndButton).assertIsDisplayed()
	}

	@Test
	fun when_theAnswerArrivesAtTheEnd_then_theListFollowsIt() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()

		content.value = content.value.copy(items = content.value.items + agentMessage(id = "reply", text = "Respuesta"))
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Message.agent("reply")).assertIsDisplayed()
	}

	@Test
	fun when_theStudentSendsWhileReadingHistory_then_theListGoesToTheirMessage() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollToTop()

		content.value = content.value.copy(
			items = content.value.items + ChatItem.User(id = QUESTION_ID, text = QUESTION, isFailed = false)
		)
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Message.bubble(QUESTION_ID)).assertIsDisplayed()
	}

	@Test
	fun when_theListFollowsAtTheEnd_then_noFrameGoesThroughTheStartOfTheConversation() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		mainClock.autoAdvance = false

		content.value = content.value.copy(
			items = content.value.items + ChatItem.User(id = QUESTION_ID, text = QUESTION, isFailed = false),
			progressLabel = FIRST_PROGRESS,
			progressAnchorId = QUESTION_ID,
			composer = content.value.composer.copy(isStoppable = true)
		)
		repeat(FOLLOW_FRAMES) {
			mainClock.advanceTimeByFrame()
			onNodeWithText(FIRST_TEXT).assertDoesNotExist()
		}
	}

	@Test
	fun when_anAnswerArrivesWhileTheListFollows_then_theListEndsOnIt() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		mainClock.autoAdvance = false

		content.value = content.value.copy(items = content.value.items + agentMessage(id = "tall", text = TALL_TEXT))
		repeat(2) { mainClock.advanceTimeByFrame() }
		content.value = content.value.copy(items = content.value.items + agentMessage(id = "reply", text = "Respuesta"))
		mainClock.autoAdvance = true
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Message.agent("reply")).assertIsDisplayed()
	}

	@Test
	fun when_theAnswerArrivesJustAboveTheEnd_then_theListFollowsIt() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollMessagesUp(NEAR_THE_END)

		content.value = content.value.copy(items = content.value.items + agentMessage(id = "reply", text = "Respuesta"))
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Message.agent("reply")).assertIsDisplayed()
	}

	@Test
	fun when_theAnswerArrivesAwayFromTheEnd_then_theListStaysWhereItIs() = runTuIndiceUiTest {
		val content = mutableStateOf(conversation(lastUseful = null))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollMessagesUp(AWAY_FROM_THE_END)

		content.value = content.value.copy(items = content.value.items + agentMessage(id = "reply", text = "Respuesta"))
		waitForIdle()

		onNodeWithTag(AssistantUiTags.Message.agent("reply")).assertDoesNotExist()
	}

	@Test
	fun when_progressAppearsAtTheFootOfATallItem_then_theListNeverGoesBackToItsStart() = runTuIndiceUiTest {
		val tall = "tall"
		val history = conversation(lastUseful = null)
		val content = mutableStateOf(history.copy(items = history.items + agentMessage(id = tall, text = TALL_TEXT)))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollMessagesDown(CONTAINER_HEIGHT * 20)
		mainClock.autoAdvance = false

		content.value = content.value.copy(progressLabel = FIRST_PROGRESS, progressAnchorId = tall)
		var top = onNodeWithTag(AssistantUiTags.Message.agent(tall)).fetchSemanticsNode().positionInRoot.y
		repeat(FOLLOW_FRAMES) {
			mainClock.advanceTimeByFrame()
			val next = onNodeWithTag(AssistantUiTags.Message.agent(tall)).fetchSemanticsNode().positionInRoot.y
			assertTrue(next <= top, "The list went back up the tall item.")
			top = next
		}
	}

	@Test
	fun when_theButtonGoesToATallLastItem_then_theListEndsAtItsFoot() = runTuIndiceUiTest {
		val history = conversation(lastUseful = null)
		val content = mutableStateOf(history.copy(items = history.items + agentMessage(id = "tall", text = TALL_TEXT)))
		setTuIndiceTestContent {
			AssistantChatConversationView(content = content.value, onAction = {}, modifier = Modifier.height(CONTAINER_HEIGHT))
		}
		waitForIdle()
		scrollMessagesDown(CONTAINER_HEIGHT * 20)
		scrollToTop()

		onNodeWithTag(AssistantUiTags.ScrollToEndButton).performClick()
		waitForIdle()

		onNodeWithTag(AssistantUiTags.ScrollToEndButton).assertDoesNotExist()
	}

	private fun waitingConversation(label: String): AssistantChat.State.Content =
		contentState(items = listOf(ChatItem.User(id = QUESTION_ID, text = QUESTION, isFailed = false)))
			.copy(
				progressLabel = label,
				progressAnchorId = QUESTION_ID,
				composer = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false, isStoppable = true)
			)

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

	// The bounds of a node stop at the list's clip, so the foot of the label is measured from its unclipped position.
	private fun ComposeUiTest.assertProgressShowsWhole() {
		val progress = onNodeWithTag(AssistantUiTags.ProgressContainer).fetchSemanticsNode()
		val list = onNodeWithTag(AssistantUiTags.MessageList).fetchSemanticsNode().boundsInRoot
		assertTrue(
			progress.positionInRoot.y + progress.size.height <= list.bottom,
			"The message list cuts off the progress label."
		)
	}

	private fun ComposeUiTest.scrollMessagesUp(distance: Dp) = scrollMessagesDown(-distance)

	private fun ComposeUiTest.scrollMessagesDown(distance: Dp) {
		val pixels = with(density) { distance.toPx() }
		onNodeWithTag(AssistantUiTags.MessageList).performSemanticsAction(SemanticsActions.ScrollBy) { scrollBy ->
			scrollBy(0f, pixels)
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
		const val FOLLOW_FRAMES = 12
		val TALL_TEXT = "Una respuesta larga. ".repeat(120)
		val NEAR_THE_END = 40.dp
		val AWAY_FROM_THE_END = 120.dp
		const val FIRST_TEXT = "Respuesta 1"
		const val LAST_TEXT = "Respuesta 12"
		const val KEYBOARD_PX = 160

		// A window too short for the notice, the suggestions and the composer at once, with the keyboard open beside them.
		const val SHORT_CONTAINER_DP = 420
		const val SHORT_KEYBOARD_PX = 200
		const val KEYBOARD_TOP_PX = SHORT_CONTAINER_DP - SHORT_KEYBOARD_PX

		// The composer's field is at least 56 dp tall at the density of these tests, which is 1 px per dp.
		const val COMPOSER_MIN_HEIGHT_PX = 56

		// A text button is at least 40 dp tall, 40 px at the density of these tests.
		const val RETRY_MIN_HEIGHT_PX = 40
		const val FAILED_MESSAGE = "No pudimos responder. Inténtalo de nuevo."
		const val GUIDED_NOTICE = "El chat abierto está en pausa; puedes usar las preguntas sugeridas"
		const val OFFER_TITLE = "Avisos de tu informe"
		const val LAST_SUGGESTION = "¿Cuál es mi ritmo de créditos?"
		const val TYPED_TEXT = "¿Puedo retirar"
		const val QUESTION_ID = "question-waiting"
		const val QUESTION = "¿Cómo va mi trimestre?"
		const val FIRST_PROGRESS = "Revisando tus materias"
		const val WAITING_PROGRESS = "Enviando tu mensaje"
		const val PHOTO_PROGRESS = "Leyendo tu foto"
		const val LAST_MESSAGE_ID = "message-$MESSAGE_COUNT"
		const val SECOND_PROGRESS = "Revisando tus evaluaciones"
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
