package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_sending
import tuindice.assistant.generated.resources.assistant_chat_title
import tuindice.assistant.generated.resources.assistant_photo_reading

// The conversation, oldest first, under the sheet's title («Asistente» after the sparkle, AssistantSheetTitleView)
// and the context chip, with the running label, notice, suggestions and composer below. The list follows its
// last item whenever the conversation changes size.
@Composable
fun AssistantChatConversationView(
	content: AssistantChat.State.Content,
	isCameraAvailable: Boolean = false,
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
		AssistantSheetTitleView(
			text = stringResource(Res.string.assistant_chat_title),
			// With the column's spacing, 16 dp below it, as below the title of ConfirmationDialog.
			modifier = Modifier.padding(bottom = TuIndiceSpacing.Medium).testTag(AssistantUiTags.ChatTitle)
		)
		content.context?.let { context ->
			AssistantChatContextChipView(item = context, onAction = onAction)
		}
		if (content.notificationOffer) {
			AssistantNotificationOfferView(onAction = onAction)
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
		// A photo being read and a message being sent have no label of their own in the state: the labels are the view's.
		val readingLabel = if (content.isExtracting) stringResource(Res.string.assistant_photo_reading) else null
		val sendingLabel = if (content.isSending) stringResource(Res.string.assistant_chat_sending) else null

		(content.progressLabel ?: readingLabel ?: sendingLabel)?.let { label ->
			AssistantProgressView(label = label)
		}
		content.notice?.let { notice ->
			AssistantChatNoticeView(notice = notice, onAction = onAction)
		}
		if (content.suggestions.isNotEmpty()) {
			AssistantChatSuggestionsView(suggestions = content.suggestions, onAction = onAction)
		}
		if (!content.isGuided) {
			AssistantChatComposerView(
				composer = content.composer,
				isCameraAvailable = isCameraAvailable,
				onAction = onAction
			)
		}
	}
}
