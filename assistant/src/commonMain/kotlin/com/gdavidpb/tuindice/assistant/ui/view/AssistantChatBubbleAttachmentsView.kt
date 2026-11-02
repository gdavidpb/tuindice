package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.presentation.model.ChatUserAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_open_photo
import tuindice.assistant.generated.resources.assistant_attachment_photo
import org.jetbrains.compose.resources.stringResource as composeStringResource

private val PHOTO_SIZE = 96.dp

// The files of a sent message, above its text: the photos in a grid of two, each one opened large when tapped, and the
// PDF files as cards under them.
@Composable
fun AssistantChatBubbleAttachmentsView(
	attachments: List<ChatUserAttachmentItem>,
	onOpenPhoto: (ByteArray) -> Unit,
	modifier: Modifier = Modifier
) {
	val photos = attachments.filter { item -> item.kind == AssistantAttachmentKind.PHOTO }
	val documents = attachments.filter { item -> item.kind == AssistantAttachmentKind.PDF }
	val photoDescription = composeStringResource(Res.string.assistant_attachment_photo)
	val openDescription = composeStringResource(Res.string.assistant_attachment_open_photo)

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.End,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		photos.chunked(2).forEach { pair ->
			Row(horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)) {
				pair.forEach { photo ->
					val image = photo.image

					Row(
						modifier = Modifier
							.size(PHOTO_SIZE)
							.clip(RoundedCornerShape(TuIndiceRadius.Medium))
							.clickable(enabled = image != null) { image?.let(onOpenPhoto) }
							.testTag(AssistantUiTags.Message.bubble(photo.name))
					) {
						AssistantChatThumbnailView(
							bytes = photo.thumbnail,
							key = photo.name,
							contentDescription = "$photoDescription. $openDescription"
						)
					}
				}
			}
		}

		documents.forEach { document ->
			AssistantChatPdfCardView(
				name = document.name,
				detail = document.detail,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}
