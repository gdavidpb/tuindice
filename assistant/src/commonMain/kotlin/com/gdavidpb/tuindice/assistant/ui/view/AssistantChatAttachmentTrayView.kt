package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The tray of the message being written: its attachments in a row above the text, scrolled sideways when they do not
// fit. The row gives the room the × of each photo reaches into, once: above the row and at its end. Between two photos
// there are 8 dp. Under the row, the reason the first attachment that failed did, so the student knows what to do.
@Composable
fun AssistantChatAttachmentTrayView(
	attachments: List<ChatAttachmentItem>,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier.padding(bottom = TuIndiceSpacing.Small)) {
		Row(
			modifier = Modifier
				.horizontalScroll(rememberScrollState())
				.padding(top = REMOVE_REACH, end = REMOVE_REACH),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			attachments.forEach { item ->
				AssistantChatAttachmentItemView(item = item, onAction = onAction)
			}
		}

		attachments.firstOrNull { item -> item.isFailed }?.failureText?.let { reason ->
			Text(
				text = reason,
				modifier = Modifier
					.testTag(AssistantUiTags.AttachmentFailure)
					.padding(top = TuIndiceSpacing.XSmall),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.error
			)
		}
	}
}
