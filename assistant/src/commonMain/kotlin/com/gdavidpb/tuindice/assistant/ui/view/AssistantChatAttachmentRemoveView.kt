package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_remove

private val REMOVE_TOUCH_SIZE = 48.dp
private val REMOVE_CIRCLE_SIZE = 22.dp
private val REMOVE_ICON_SIZE = 14.dp

// The × in the corner of an attachment of the tray: a round mark in a 48 dp touch target, which takes the attachment
// out.
@Composable
fun AssistantChatAttachmentRemoveView(
	name: String,
	onRemove: () -> Unit,
	modifier: Modifier = Modifier
) {
	val description = stringResource(Res.string.assistant_attachment_remove, name)

	IconButton(
		onClick = onRemove,
		modifier = modifier
			.size(REMOVE_TOUCH_SIZE)
			.testTag(AssistantUiTags.AttachmentRemove)
	) {
		Box(
			modifier = Modifier
				.size(REMOVE_CIRCLE_SIZE)
				.background(MaterialTheme.colorScheme.onSurface, CircleShape),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Outlined.Close,
				contentDescription = description,
				modifier = Modifier.size(REMOVE_ICON_SIZE),
				tint = MaterialTheme.colorScheme.surface
			)
		}
	}
}
