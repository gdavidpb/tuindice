package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.presentation.model.asString

// A pause, a suspension or a ban: what it is and until when, and «Apelar» when the standing allows an appeal.
@Composable
fun AssistantRestrictedNoticeView(
	notice: ChatNoticeItem.Restricted,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Notice)) {
		Text(
			text = notice.message.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		if (notice.canAppeal) {
			AssistantAppealActionView(onAppeal = { text -> onAction(AssistantChat.Action.Appeal(text)) })
		}
	}
}
