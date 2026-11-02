package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.ChatEntranceLedger
import com.gdavidpb.tuindice.assistant.ui.style.planAgentEntrance
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnScroll
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

// The keyboard's insets, which the list follows as the keyboard opens and closes. Null, the default, is the keyboard's
// own; a test provides its own, to open and close the keyboard on the test clock.
internal val LocalChatKeyboardInsets = staticCompositionLocalOf<WindowInsets?> { null }

// The conversation, oldest first, under the screen's header,
// with the running label in the message list and the notice, suggestions and composer below.
// The list follows a new last item when the student sent it or was at the end, and its last item when the keyboard
// moves. A change inside an item leaves the list where it is. A proposal that arrives is shown up to its buttons, or
// its outcome once resolved.
//
// Motion: what arrives while the screen is open enters with the chat's motion (AssistantChatMotion); what is there when
// it opens, the history and the notice simply are. Each place that comes and goes is an AssistantChatSlotView,
// which carries its own 8 dp of separation, so the column does not space its children.
@Composable
fun AssistantChatConversationView(
	content: AssistantChat.State.Content,
	isCameraAvailable: Boolean = false,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
	val listState = rememberLazyListState()
	val focusManager = LocalFocusManager.current
	val keyboard = LocalChatKeyboardInsets.current ?: WindowInsets.ime
	val progressAtEnd = content.progressLabel != null && content.progressAnchorId == content.items.lastOrNull()?.id
	val ledger = remember { ChatEntranceLedger(content.items.map(ChatItem::id)) }
	FollowConversation(listState = listState, items = content.items, progressAtEnd = progressAtEnd)
	FollowProgressWhenAtEnd(listState = listState, visible = content.progressLabel != null)

	FollowLastItemOnKeyboard(
		listState = listState,
		items = content.items,
		progressVisible = progressAtEnd,
		ime = keyboard
	)

	// The column stays above the keyboard, or above the home indicator when there is none. The composer stays at the foot
	// and the conversation takes the height above it. The screen consumes the shell's insets, so the keyboard contributes
	// only the remaining bottom inset. Dragging the list hides the keyboard, as in any scroll container
	// (dismissKeyboardOnScroll).
	Column(
		modifier = modifier
			.fillMaxWidth()
			.windowInsetsPadding(keyboard.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom))
			.dismissKeyboardOnScroll(focusManager)
	) {
		AssistantChatRegion(
			hasMessages = content.items.isNotEmpty() || content.progressLabel != null,
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			above = { AssistantChatAboveMessages(content = content, onAction = onAction) },
			messages = {
				AssistantChatMessagesPlace(listState = listState, snackbarHostState = snackbarHostState) {
					AssistantChatMessageListView(
						items = content.items,
						progress = ConversationProgress(label = content.progressLabel, anchorId = content.progressAnchorId),
						ledger = ledger,
						listState = listState,
						onAction = onAction
					)
				}
			},
			below = { AssistantChatStatusView(content = content, ledger = ledger, onAction = onAction) }
		)
		if (!content.isGuided) {
			AssistantChatComposerView(
				composer = content.composer,
				isCameraAvailable = isCameraAvailable,
				onAction = onAction,
				modifier = Modifier.padding(top = TuIndiceSpacing.Medium)
			)
		}
	}
}

// The region holds the offer, messages and suggestions. The messages take the height left by the other parts. When the
// parts leave none, the region scrolls as one: the offer, notice and suggestions stay in reach while the composer is
// pinned under it. The messages keep a minimum then, so they never vanish behind the parts around them.
//
// A region with messages follows its end, as the list follows its last item: whenever what it holds or the height it
// has changes (a notice arrives, the keyboard opens), its foot (the notice and its retry, the last
// suggestion) sits whole right above the composer, and what does not fit leaves at the top. Each frame of the keyboard
// moves it, so it is a jump, not an animation; a scroll of the person's own changes neither, so it is not undone. An
// empty chat starts at its top instead: the offer of the notifications, which only an empty chat shows, stays in view.
@Composable
private fun AssistantChatRegion(
	hasMessages: Boolean,
	above: @Composable () -> Unit,
	messages: @Composable () -> Unit,
	below: @Composable () -> Unit,
	modifier: Modifier = Modifier
) {
	BoxWithConstraints(modifier = modifier) {
		val regionHeight = maxHeight
		val scrollState = rememberScrollState()

		LaunchedEffect(scrollState, hasMessages) {
			if (!hasMessages) return@LaunchedEffect

			snapshotFlow { scrollState.maxValue }
				.distinctUntilChanged()
				.collectLatest { scrollState.scrollTo(it) }
		}

		Layout(
			content = {
				Column { above() }
				messages()
				Column { below() }
			},
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(scrollState)
		) { measurables, constraints ->
			val width = constraints.maxWidth
			// The height of the region comes from the box around it: a scrolling layout is measured without a height limit.
			val top = measurables[0].measure(Constraints(maxWidth = width))
			val bottom = measurables[2].measure(Constraints(maxWidth = width))
			val remaining = regionHeight.roundToPx() - top.height - bottom.height
			val messagesHeight = when {
				remaining >= 0 -> remaining
				hasMessages -> MinimumMessagesHeight.roundToPx()
				else -> 0
			}
			val middle = measurables[1].measure(Constraints.fixed(width, messagesHeight))
			val height = top.height + middle.height + bottom.height

			layout(width, height) {
				top.place(x = 0, y = 0)
				middle.place(x = 0, y = top.height)
				bottom.place(x = 0, y = top.height + middle.height)
			}
		}
	}
}

// The notification offer above the messages, while the chat shows it.
@Composable
private fun AssistantChatAboveMessages(
	content: AssistantChat.State.Content,
	onAction: (AssistantChat.Action) -> Unit
) {
	AssistantChatSlotView(visible = content.notificationOffer) {
		AssistantNotificationOfferView(onAction = onAction)
	}
}

// The running turn's label and the item it goes under. Equal values keep the list from composing again.
private data class ConversationProgress(val label: String?, val anchorId: String?)

// The messages, with the chat's snackbar and the button that goes to their end floating over their foot: they sit
// right above what follows the messages, so they take no room in any mode while they do not show.
@Composable
private fun AssistantChatMessagesPlace(
	listState: LazyListState,
	snackbarHostState: SnackbarHostState,
	messages: @Composable () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = TuIndiceSpacing.Medium)
	) {
		messages()
		AssistantChatScrollToEndButtonView(
			listState = listState,
			modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = TuIndiceSpacing.Small)
		)
		AssistantChatSnackbarView(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
	}
}

// The least the messages keep when the parts around them fill the region: a conversation with messages stays in reach.
private val MinimumMessagesHeight = 160.dp

// The values come one by one, not as the content: a change of the composer alone leaves the list composed.
@Composable
private fun AssistantChatMessageListView(
	items: List<ChatItem>,
	progress: ConversationProgress,
	ledger: ChatEntranceLedger,
	listState: LazyListState,
	onAction: (AssistantChat.Action) -> Unit
) {
	// Reported to the composition probe of the tests only: it shows whether a change composes the list again.
	ReportComposition(name = "message-list")
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current

	LazyColumn(
		modifier = Modifier.fillMaxSize().testTag(AssistantUiTags.MessageList),
		state = listState,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large)
	) {
		if (items.isEmpty()) {
			item { AssistantChatInlineProgressView(label = progress.label) }
		}
		items(items = items, key = ChatItem::id) { item ->
			// Looked up once, when the item is first composed: an item that scrolls back into view is not new.
			val isNew = remember(item.id) { ledger.isNew(item.id) }
			// Items that change place (one removed above them) glide to it; the entrance is the view's own, so the list
			// adds neither a fade in nor a fade out.
			val itemModifier = if (animationsEnabled) {
				Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
			} else {
				Modifier
			}

			Column(modifier = itemModifier) {
				when (item) {
					is ChatItem.User -> AssistantUserBubbleView(item = item, animateEntrance = isNew)

					is ChatItem.Agent -> AssistantAgentMessageView(item = item, onAction = onAction, animateEntrance = isNew)
				}
				if (item.id == progress.anchorId) AssistantChatInlineProgressView(label = progress.label)
			}
		}
	}
}

// What follows the messages: the notice and suggestions, each in a place that comes and goes.
@Composable
private fun AssistantChatStatusView(
	content: AssistantChat.State.Content,
	ledger: ChatEntranceLedger,
	onAction: (AssistantChat.Action) -> Unit
) {
	val shownNotice = rememberLastNonNull(content.notice)

	AssistantChatSlotView(visible = content.notice != null) {
		shownNotice?.let { AssistantChatNoticeView(notice = it, onAction = onAction) }
	}
	AssistantChatSuggestionsSlotView(content = content, ledger = ledger, onAction = onAction)
}

// The suggestions after the messages, in a place of their own that comes and goes.
@Composable
private fun AssistantChatSuggestionsSlotView(
	content: AssistantChat.State.Content,
	ledger: ChatEntranceLedger,
	onAction: (AssistantChat.Action) -> Unit
) {
	val shownSuggestions = rememberLastNonNull(content.suggestions.takeIf { it.isNotEmpty() })

	// Suggestions that are on screen when the chat opens are there; they enter chip by chip only once they have gone
	// away and come back (the guided mode starts with them).
	val suggestionsHaveGone = remember { mutableStateOf(false) }

	LaunchedEffect(content.suggestions.isEmpty()) {
		if (content.suggestions.isEmpty()) suggestionsHaveGone.value = true
	}

	// The suggestions follow the answer that is typing in: they enter when its actions do.
	val lastAgent = content.items.lastOrNull() as? ChatItem.Agent
	// The plan of the answer's entrance depends on its parts only: it is planned once for them, not on each keystroke.
	val lastAgentPlan = remember(lastAgent?.parts) { lastAgent?.let { planAgentEntrance(it.parts) } }
	val suggestionsLeadMillis = lastAgent
		?.takeIf { ledger.arrivedAfterOpen(it.id) }
		?.let { lastAgentPlan?.actionsDelay }
		?: 0

	// Once the student writes their own question the suggestions give the conversation their room: they fade and shrink
	// away while the composer has text, and grow back if it is emptied. Their own entrance stays chip by chip; only the
	// way back from writing grows the slot.
	// Guided mode hides the composer, so a draft restored from a guided_only refusal cannot be edited or sent there.
	// Keep the screen's suggested questions available while that hidden draft is preserved for when the mode returns.
	val isWriting = !content.isGuided && content.composer.text.isNotBlank()
	val hiddenByWriting = remember { mutableStateOf(false) }

	LaunchedEffect(isWriting, content.suggestions.isEmpty()) {
		if (isWriting) hiddenByWriting.value = true else if (content.suggestions.isEmpty()) hiddenByWriting.value = false
	}

	AssistantChatSlotView(
		visible = content.suggestions.isNotEmpty() && !isWriting,
		fadeOnly = !isWriting && !hiddenByWriting.value
	) {
		shownSuggestions?.let {
			AssistantChatSuggestionsView(
				suggestions = it,
				onAction = onAction,
				entranceLeadMillis = suggestionsLeadMillis.takeIf { suggestionsHaveGone.value }
			)
		}
	}
}
