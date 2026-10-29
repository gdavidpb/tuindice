package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.hasIntentCard
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.ChatEntranceLedger
import com.gdavidpb.tuindice.assistant.ui.style.planAgentEntrance
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnScroll
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_sending
import tuindice.assistant.generated.resources.assistant_chat_title
import tuindice.assistant.generated.resources.assistant_photo_reading

// The keyboard's insets, which the list follows as the keyboard opens and closes. Null, the default, is the keyboard's
// own; a test provides its own, to open and close the keyboard on the test clock.
internal val LocalChatKeyboardInsets = staticCompositionLocalOf<WindowInsets?> { null }

// The conversation, oldest first, under the sheet's title («Asistente» after the sparkle, AssistantSheetTitleView)
// and the context chip, with the running label, notice, suggestions and composer below. The list follows its
// last item when a new one arrives, or another one ends the conversation, and when the keyboard opens or closes. A
// change inside an item leaves the list where it is. A proposal that arrives is shown up to its buttons, or its
// outcome once resolved.
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
	val keyboard = LocalChatKeyboardInsets.current ?: WindowInsets.ime
	val lastItem = content.items.lastOrNull()
	val ledger = remember { ChatEntranceLedger(content.items.map(ChatItem::id)) }
	LaunchedEffect(followKeyOf(content.items)) {
		if (content.items.isNotEmpty()) {
			listState.followLastItem(
				lastIndex = content.items.lastIndex,
				showsItsEnd = lastItem?.hasIntentCard == true
			)
		}
	}

	FollowLastItemOnKeyboard(listState = listState, items = content.items, ime = keyboard)

	// The column stays above the keyboard, or above the home indicator when there is none. Its title sits at the top and
	// its composer at the foot; what lies between them is the region of the conversation, which takes the height they
	// leave. So the composer keeps its place right above the keyboard, and the region scrolls when what it holds does not
	// fit. The keyboard's inset is real inside the sheet, and the sheet pads only its top, so this is the only bottom
	// inset of the conversation. Dragging the list hides the keyboard, as in any scroll container
	// (dismissKeyboardOnScroll).
	Column(
		modifier = modifier
			.fillMaxWidth()
			.windowInsetsPadding(keyboard.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom))
			.dismissKeyboardOnScroll(focusManager)
	) {
		AssistantSheetTitleView(
			text = stringResource(Res.string.assistant_chat_title),
			// With the 16 dp that follows every place of the column, 32 dp below it, as below the title of ConfirmationDialog.
			modifier = Modifier.padding(bottom = TuIndiceSpacing.Medium).testTag(AssistantUiTags.ChatTitle)
		)
		AssistantChatRegion(
			hasMessages = content.items.isNotEmpty(),
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			above = { AssistantChatAboveMessages(content = content, onAction = onAction) },
			messages = {
				AssistantChatMessagesPlace(
					content = content,
					ledger = ledger,
					listState = listState,
					snackbarHostState = snackbarHostState,
					onAction = onAction
				)
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

// The region from the context chip down to the last suggestion. The messages take the height that the parts around them
// leave, so a region whose parts fit lays out the way a column does. When they leave none, the parts hold more than the
// region, and the region scrolls as one: the chip, the notice and the suggestions stay in reach while the composer is
// pinned under it. The messages keep a minimum then, so they never vanish behind the parts around them.
//
// A region with messages follows its end, as the list follows its last item: whenever what it holds or the height it
// has changes (a notice arrives, the keyboard opens), its foot (the notice and its retry, the running label, the last
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

// The places above the messages: the context chip, and the offer of the notifications while the chat shows it.
@Composable
private fun AssistantChatAboveMessages(
	content: AssistantChat.State.Content,
	onAction: (AssistantChat.Action) -> Unit
) {
	val shownContext = rememberLastNonNull(content.context)

	AssistantChatSlotView(visible = content.context != null) {
		shownContext?.let { AssistantChatContextChipView(item = it, onAction = onAction) }
	}
	AssistantChatSlotView(visible = content.notificationOffer) {
		AssistantNotificationOfferView(onAction = onAction)
	}
}

// The messages, with the sheet's snackbar floating over their foot: it sits right above what follows the messages,
// so it takes no room in any mode while no notice shows.
@Composable
private fun AssistantChatMessagesPlace(
	content: AssistantChat.State.Content,
	ledger: ChatEntranceLedger,
	listState: LazyListState,
	snackbarHostState: SnackbarHostState,
	onAction: (AssistantChat.Action) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = TuIndiceSpacing.Medium)
	) {
		AssistantChatMessageListView(
			items = content.items,
			ledger = ledger,
			listState = listState,
			onAction = onAction
		)
		AssistantChatSnackbarView(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
	}
}

// The least the messages keep when the parts around them fill the region: a conversation with messages stays in reach.
private val MinimumMessagesHeight = 160.dp

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

// The list follows its last item as the keyboard opens and closes, in each frame of its animation. The inset is read
// outside composition, so those frames compose nothing; a follow cancels the one before it. Other changes of the
// column's height (a suggestion, a notice) leave the list where it is.
@Composable
private fun FollowLastItemOnKeyboard(listState: LazyListState, items: List<ChatItem>, ime: WindowInsets) {
	val latestItems by rememberUpdatedState(items)
	val density = LocalDensity.current

	// The first value is not followed: the conversation's effect follows it when the conversation is composed.
	LaunchedEffect(listState, ime) {
		snapshotFlow { ime.getBottom(density) }
			.distinctUntilChanged()
			.drop(1)
			.collectLatest {
				val current = latestItems

				if (current.isNotEmpty()) {
					listState.followLastItem(
						lastIndex = current.lastIndex,
						showsItsEnd = current.last().hasIntentCard
					)
				}
			}
	}
}

// Shows the last item from its start. A last item that has an intent card is shown up to its end: a proposal up to
// its buttons, or its outcome once resolved. What of it is still below the list is scrolled too, so the start of such a
// message may leave the list.
private suspend fun LazyListState.followLastItem(lastIndex: Int, showsItsEnd: Boolean) {
	animateScrollToItem(lastIndex)
	if (!showsItsEnd) return

	val layout = layoutInfo
	val lastItemInfo = layout.visibleItemsInfo.lastOrNull { it.index == lastIndex }
	val hiddenBelow = lastItemInfo?.let { it.offset + it.size - layout.viewportEndOffset } ?: 0

	if (hiddenBelow > 0) animateScrollBy(hiddenBelow.toFloat())
}

// What the list follows its last item by: how many items there are, and which one is last. A change inside an item
// keeps both, so it does not move the list; a new item, or another one at the end, changes them.
internal fun followKeyOf(items: List<ChatItem>): Pair<Int, String?> = items.size to items.lastOrNull()?.id

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
				entranceLeadMillis = suggestionsLeadMillis.takeIf { suggestionsHaveGone.value }
			)
		}
	}
}
