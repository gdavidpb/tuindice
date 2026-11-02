package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The tray of the message being written: its attachments in a row above the text, scrolled sideways when they
// do not fit.
@Composable
fun AssistantChatAttachmentTrayView(
	attachments: List<ChatAttachmentItem>,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.horizontalScroll(rememberScrollState())
			.padding(bottom = TuIndiceSpacing.Small),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
	) {
		attachments.forEach { item ->
			AssistantChatAttachmentItemView(item = item, onAction = onAction)
		}
	}
}
