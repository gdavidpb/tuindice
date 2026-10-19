package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.ChatEntranceLedger
import com.gdavidpb.tuindice.assistant.ui.style.planAgentEntrance
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnScroll
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_sending
import tuindice.assistant.generated.resources.assistant_chat_title
import tuindice.assistant.generated.resources.assistant_photo_reading

// The conversation, oldest first, under the sheet's title («Asistente» after the sparkle, AssistantSheetTitleView)
// and the context chip, with the running label, notice, suggestions and composer below. The list follows its
// last item whenever the conversation changes size.
//
// Motion: what arrives while the sheet is open enters with the chat's motion (AssistantChatMotion); what is there when
// it opens, the history, the chip, the notice, simply is. Each place that comes and goes is an AssistantChatSlotView,
// which carries its own 16 dp of separation, so the column does not space its children.
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
	val lastItem = content.items.lastOrNull()
	val ledger = remember { ChatEntranceLedger(content.items.map(ChatItem::id)) }

	LaunchedEffect(content.items.size, lastItem) {
		if (content.items.isNotEmpty()) {
			listState.animateScrollToItem(content.items.lastIndex)
		}
	}

	// The column stays above the keyboard (imePadding): the list gives up the room, and the composer keeps its place.
	// Dragging the list hides the keyboard, as in any scroll container (dismissKeyboardOnScroll).
	Column(modifier = modifier.fillMaxWidth().imePadding().dismissKeyboardOnScroll(focusManager)) {
		AssistantSheetTitleView(
			text = stringResource(Res.string.assistant_chat_title),
			// With the 16 dp that follows every place of the column, 32 dp below it, as below the title of ConfirmationDialog.
			modifier = Modifier.padding(bottom = TuIndiceSpacing.Medium).testTag(AssistantUiTags.ChatTitle)
		)
		val shownContext = rememberLastNonNull(content.context)

		AssistantChatSlotView(visible = content.context != null) {
			shownContext?.let { AssistantChatContextChipView(item = it, onAction = onAction) }
		}
		AssistantChatSlotView(visible = content.notificationOffer) {
			AssistantNotificationOfferView(onAction = onAction)
		}
		// The sheet's snackbar floats over the foot of the messages, right above what follows them (the composer, or the
		// suggestions of the guided mode), so it takes no room in any mode while no notice shows.
		Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = TuIndiceSpacing.Medium)) {
			AssistantChatMessageListView(
				items = content.items,
				ledger = ledger,
				listState = listState,
				onAction = onAction
			)
			AssistantChatSnackbarView(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
		}
		AssistantChatStatusView(content = content, ledger = ledger, onAction = onAction)
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

@Composable
private fun AssistantChatMessageListView(
	items: List<ChatItem>,
	ledger: ChatEntranceLedger,
	listState: LazyListState,
	onAction: (AssistantChat.Action) -> Unit
) {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current

	LazyColumn(
		modifier = Modifier.fillMaxSize().testTag(AssistantUiTags.MessageList),
		state = listState,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large)
	) {
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

			when (item) {
				is ChatItem.User -> AssistantUserBubbleView(item = item, modifier = itemModifier, animateEntrance = isNew)

				is ChatItem.Agent -> AssistantAgentMessageView(
					item = item,
					onAction = onAction,
					modifier = itemModifier,
					animateEntrance = isNew
				)
			}
		}
	}
}

// What follows the messages: the running label, the notice and the suggestions, each in a place that comes and goes. A
// photo being read and a message being sent have no label of their own in the state: the labels are the view's.
@Composable
private fun AssistantChatStatusView(
	content: AssistantChat.State.Content,
	ledger: ChatEntranceLedger,
	onAction: (AssistantChat.Action) -> Unit
) {
	val readingLabel = if (content.isExtracting) stringResource(Res.string.assistant_photo_reading) else null
	val sendingLabel = if (content.isSending) stringResource(Res.string.assistant_chat_sending) else null
	val progressLabel = content.progressLabel ?: readingLabel ?: sendingLabel
	val shownLabel = rememberLastNonNull(progressLabel)
	val shownNotice = rememberLastNonNull(content.notice)
	val shownSuggestions = rememberLastNonNull(content.suggestions.takeIf { it.isNotEmpty() })

	// Suggestions that are on screen when the sheet opens are there; they enter chip by chip only once they have gone
	// away and come back (the guided mode starts with them).
	val suggestionsHaveGone = remember { mutableStateOf(false) }

	LaunchedEffect(content.suggestions.isEmpty()) {
		if (content.suggestions.isEmpty()) suggestionsHaveGone.value = true
	}

	// The suggestions follow the answer that is typing in: they enter when its actions do.
	val suggestionsLeadMillis = (content.items.lastOrNull() as? ChatItem.Agent)
		?.takeIf { ledger.arrivedAfterOpen(it.id) }
		?.let { planAgentEntrance(it.parts).actionsDelay }
		?: 0

	AssistantChatSlotView(visible = progressLabel != null) {
		shownLabel?.let { AssistantProgressView(label = it) }
	}
	AssistantChatSlotView(visible = content.notice != null) {
		shownNotice?.let { AssistantChatNoticeView(notice = it, onAction = onAction) }
	}
	AssistantChatSlotView(visible = content.suggestions.isNotEmpty(), fadeOnly = true) {
		shownSuggestions?.let {
			AssistantChatSuggestionsView(
				suggestions = it,
				onAction = onAction,
				animateEntrance = suggestionsHaveGone.value,
				leadMillis = suggestionsLeadMillis
			)
		}
	}
}
