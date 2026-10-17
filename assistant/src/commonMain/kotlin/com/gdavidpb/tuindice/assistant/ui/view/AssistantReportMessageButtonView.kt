package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_report
import tuindice.assistant.generated.resources.assistant_chat_report_harmful
import tuindice.assistant.generated.resources.assistant_chat_report_inappropriate
import tuindice.assistant.generated.resources.assistant_chat_report_incorrect
import tuindice.assistant.generated.resources.assistant_chat_report_other

// «Reportar»: a menu with the four reasons. Choosing one reports the message, without a comment.
@Composable
fun AssistantReportMessageButtonView(
	messageId: String,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val isMenuVisible = remember { mutableStateOf(false) }

	Box(modifier = modifier) {
		IconButton(
			onClick = { isMenuVisible.value = true },
			modifier = Modifier.testTag(AssistantUiTags.Message.report(messageId))
		) {
			Icon(
				imageVector = Icons.Outlined.Flag,
				contentDescription = stringResource(Res.string.a11y_assistant_report),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		DropdownMenu(
			expanded = isMenuVisible.value,
			onDismissRequest = { isMenuVisible.value = false }
		) {
			ReportReason.entries.forEach { reason ->
				DropdownMenuItem(
					text = { Text(text = stringResource(reason.labelResource())) },
					onClick = {
						isMenuVisible.value = false
						onAction(AssistantChat.Action.ReportMessage(messageId, reason, comment = null))
					},
					modifier = Modifier.testTag(AssistantUiTags.Message.reportReason(messageId, reason))
				)
			}
		}
	}
}

private fun ReportReason.labelResource(): StringResource =
	when (this) {
		ReportReason.INCORRECT -> Res.string.assistant_chat_report_incorrect
		ReportReason.INAPPROPRIATE -> Res.string.assistant_chat_report_inappropriate
		ReportReason.HARMFUL -> Res.string.assistant_chat_report_harmful
		ReportReason.OTHER -> Res.string.assistant_chat_report_other
	}
