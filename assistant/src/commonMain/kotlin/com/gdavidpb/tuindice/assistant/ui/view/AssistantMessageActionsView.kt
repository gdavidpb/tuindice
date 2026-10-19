package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.model.AssistantMessageActionSizes
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_copy
import tuindice.assistant.generated.resources.a11y_assistant_not_useful
import tuindice.assistant.generated.resources.a11y_assistant_useful

// The actions under an agent message, in order: Copiar, the two thumbs, and Reportar. They are small and close together
// (AssistantMessageActionSizes), so they do not compete with the message; the thumb the student chose is filled in the
// theme's inverse surface, and a reported message shows its flag filled in the error colour.
@Composable
fun AssistantMessageActionsView(
	messageId: String,
	useful: Boolean?,
	reported: Boolean,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(modifier = modifier.fillMaxWidth()) {
		IconButton(
			onClick = { onAction(AssistantChat.Action.CopyMessage(messageId)) },
			modifier = Modifier
				.size(AssistantMessageActionSizes.Button)
				.testTag(AssistantUiTags.Message.copy(messageId))
		) {
			Icon(
				imageVector = Icons.Outlined.ContentCopy,
				contentDescription = stringResource(Res.string.a11y_assistant_copy),
				modifier = Modifier.size(AssistantMessageActionSizes.Icon),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		listOf(true, false).forEach { isUseful ->
			val isSelected = useful == isUseful
			IconButton(
				onClick = { onAction(AssistantChat.Action.RateMessage(messageId, isUseful)) },
				modifier = Modifier
					.size(AssistantMessageActionSizes.Button)
					.testTag(thumbTag(messageId, isUseful))
					.semantics { selected = isSelected }
			) {
				AssistantStateIconView(
					isSelected = isSelected,
					selectedIcon = if (isUseful) Icons.Filled.ThumbUp else Icons.Filled.ThumbDown,
					unselectedIcon = if (isUseful) Icons.Outlined.ThumbUp else Icons.Outlined.ThumbDown,
					selectedTint = MaterialTheme.colorScheme.inverseSurface,
					unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant,
					contentDescription = stringResource(
						if (isUseful) Res.string.a11y_assistant_useful else Res.string.a11y_assistant_not_useful
					)
				)
			}
		}
		AssistantReportMessageButtonView(messageId = messageId, isReported = reported, onAction = onAction)
	}
}

private fun thumbTag(messageId: String, isUseful: Boolean): String =
	if (isUseful) AssistantUiTags.Message.useful(messageId) else AssistantUiTags.Message.notUseful(messageId)
