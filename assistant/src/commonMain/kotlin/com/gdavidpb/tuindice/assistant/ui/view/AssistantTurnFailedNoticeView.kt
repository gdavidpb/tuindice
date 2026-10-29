package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_retry

// The last turn failed: its message, and «Reintentar» when the service allows another try.
@Composable
fun AssistantTurnFailedNoticeView(
	notice: ChatNoticeItem.TurnFailed,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.NoticeTurnFailed)) {
		Text(
			text = notice.message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		if (notice.canRetry) {
			TextButton(
				onClick = { onAction(AssistantChat.Action.RetryTurn) },
				modifier = Modifier.testTag(AssistantUiTags.NoticeRetry)
			) {
				Text(text = stringResource(Res.string.assistant_retry))
			}
		}
	}
}
