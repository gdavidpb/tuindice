package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_photo

private val TILE_SIZE = 56.dp

// How far the × reaches past the photo's top and end edges. Its 48 dp touch target is centred on the photo's top-right
// corner, so half of it lies outside the photo. The tray gives that room once, to its row.
internal val REMOVE_REACH = 24.dp

// One photo of the tray: its thumbnail, with its × on the corner. While it is read or sent, or when it failed, the veil
// covers it; the × is drawn over the veil, so a failed photo can always be taken out.
@Composable
fun AssistantChatAttachmentItemView(
	item: ChatAttachmentItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Box(modifier = modifier.testTag(AssistantUiTags.AttachmentTray)) {
		Box(
			modifier = Modifier
				.size(TILE_SIZE)
				.clip(RoundedCornerShape(TuIndiceRadius.Medium))
				.background(MaterialTheme.colorScheme.surfaceVariant)
		) {
			AssistantChatThumbnailView(
				bytes = item.thumbnail,
				cacheKey = item.localId,
				contentDescription = stringResource(Res.string.assistant_attachment_photo)
			)
		}

		AssistantChatAttachmentStateView(
			item = item,
			onRetry = { onAction(AssistantChat.Action.RetryAttachment(localId = item.localId)) },
			modifier = Modifier.matchParentSize()
		)

		AssistantChatAttachmentRemoveView(
			name = item.name,
			onRemove = { onAction(AssistantChat.Action.RemoveAttachment(localId = item.localId)) },
			modifier = Modifier
				.align(Alignment.TopEnd)
				.offset(x = REMOVE_REACH, y = -REMOVE_REACH)
		)
	}
}
