package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags

// A warning, a cooldown, a suspension or a ban: its message, and «Apelar» when an appeal is allowed.
@Composable
fun AssistantRestrictedNoticeView(
	notice: ChatNoticeItem.Restricted,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Notice)) {
		Text(
			text = notice.message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		if (notice.canAppeal) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
