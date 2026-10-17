package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The conversation, oldest first, with the context chip above and the running label, notice, suggestions and
// composer below. The list follows its last item whenever the conversation changes size.
@Composable
fun AssistantChatConversationView(
	content: AssistantChat.State.Content,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val listState = rememberLazyListState()
	val lastItem = content.items.lastOrNull()

	LaunchedEffect(content.items.size, lastItem) {
		if (content.items.isNotEmpty()) {
			listState.animateScrollToItem(content.items.lastIndex)
		}
	}

	Column(
		modifier = modifier.fillMaxWidth().imePadding(),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		content.context?.let { context ->
			AssistantChatContextChipView(item = context, onAction = onAction)
		}
		LazyColumn(
			modifier = Modifier.weight(1f).fillMaxWidth().testTag(AssistantUiTags.MessageList),
			state = listState,
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large)
		) {
			items(items = content.items, key = ChatItem::id) { item ->
				when (item) {
					is ChatItem.User -> AssistantUserBubbleView(item = item)
					is ChatItem.Agent -> AssistantAgentMessageView(item = item, onAction = onAction)
				}
			}
		}
		content.progressLabel?.let { label ->
			AssistantProgressView(label = label)
		}
		content.notice?.let { notice ->
			AssistantChatNoticeView(notice = notice, onAction = onAction)
		}
		if (content.suggestions.isNotEmpty()) {
			AssistantChatSuggestionsView(suggestions = content.suggestions, onAction = onAction)
		}
		if (!content.isGuided) {
			AssistantChatComposerView(composer = content.composer, onAction = onAction)
		}
	}
}
