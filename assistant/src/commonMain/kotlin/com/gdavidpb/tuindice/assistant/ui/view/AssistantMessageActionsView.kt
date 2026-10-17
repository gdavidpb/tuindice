package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_copy
import tuindice.assistant.generated.resources.a11y_assistant_not_useful
import tuindice.assistant.generated.resources.a11y_assistant_useful

// The actions under an agent message, in order: Copiar, the two thumbs, and Reportar. The thumb the student chose
// is filled and in the accent.
@Composable
fun AssistantMessageActionsView(
	messageId: String,
	useful: Boolean?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		IconButton(
			onClick = { onAction(AssistantChat.Action.CopyMessage(messageId)) },
			modifier = Modifier.testTag(AssistantUiTags.Message.copy(messageId))
		) {
			Icon(
				imageVector = Icons.Outlined.ContentCopy,
				contentDescription = stringResource(Res.string.a11y_assistant_copy),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		listOf(true, false).forEach { isUseful ->
			val isSelected = useful == isUseful
			val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
			IconButton(
				onClick = { onAction(AssistantChat.Action.RateMessage(messageId, isUseful)) },
				modifier = Modifier
					.testTag(thumbTag(messageId, isUseful))
					.semantics { selected = isSelected }
			) {
				Icon(
					imageVector = thumbIcon(isUseful = isUseful, isSelected = isSelected),
					contentDescription = stringResource(
						if (isUseful) Res.string.a11y_assistant_useful else Res.string.a11y_assistant_not_useful
					),
					tint = tint
				)
			}
		}
		AssistantReportMessageButtonView(messageId = messageId, onAction = onAction)
	}
}

private fun thumbTag(messageId: String, isUseful: Boolean): String =
	if (isUseful) AssistantUiTags.Message.useful(messageId) else AssistantUiTags.Message.notUseful(messageId)

private fun thumbIcon(isUseful: Boolean, isSelected: Boolean): ImageVector =
	when {
		isUseful && isSelected -> Icons.Filled.ThumbUp
		isUseful -> Icons.Outlined.ThumbUp
		isSelected -> Icons.Filled.ThumbDown
		else -> Icons.Outlined.ThumbDown
	}
