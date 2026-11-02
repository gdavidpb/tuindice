package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_photo

private val TILE_SIZE = 56.dp
private val PDF_WIDTH = 200.dp

// The × takes its corner of the attachment: half of it sits over the corner, and the padding around the attachment
// holds that half.
private val CORNER_INSET = 12.dp

// The × takes its touch target (36 dp of it lies over the card's end), so the name of a PDF keeps clear of it.
private val REMOVE_CLEARANCE = 36.dp

// One attachment of the tray: a photo's thumbnail, or a PDF card, with its × in the corner. While it is read or sent,
// or when it failed, the veil covers it; the × is drawn over the veil, so a failed attachment can always be taken out.
@Composable
fun AssistantChatAttachmentItemView(
	item: ChatAttachmentItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.testTag(AssistantUiTags.AttachmentTray)
			.padding(CORNER_INSET)
	) {
		if (item.kind == AssistantAttachmentKind.PHOTO) {
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
		} else {
			AssistantChatPdfCardView(
				name = item.name,
				detail = item.detail,
				modifier = Modifier.width(PDF_WIDTH),
				endPadding = REMOVE_CLEARANCE
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
				.offset(x = CORNER_INSET, y = -CORNER_INSET)
		)
	}
}
