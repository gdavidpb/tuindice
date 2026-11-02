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

// A photo the dialog opens large: its bytes, and the key the large decoding is kept under.
internal class OpenedPhoto(val bytes: ByteArray, val cacheKey: String)

// The files of a sent message, above its text: the photos in a grid of two, each one opened large when tapped, and the
// PDF files as cards under them. A photo whose image is no longer kept opens its thumbnail large.
@Composable
fun AssistantChatBubbleAttachmentsView(
	messageId: String,
	attachments: List<ChatUserAttachmentItem>,
	onOpenPhoto: (image: ByteArray, cacheKey: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val photos = attachments.withIndex().filter { (_, item) -> item.kind == AssistantAttachmentKind.PHOTO }
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
				pair.forEach { (index, photo) ->
					val cacheKey = "$messageId-$index"
					val large = photo.image ?: photo.thumbnail

					Row(
						modifier = Modifier
							.size(PHOTO_SIZE)
							.clip(RoundedCornerShape(TuIndiceRadius.Medium))
							.clickable(enabled = large != null) {
								large?.let { bytes -> onOpenPhoto(bytes, "open-$cacheKey") }
							}
							.testTag(AssistantUiTags.Message.attachment(messageId, index))
					) {
						AssistantChatThumbnailView(
							bytes = photo.thumbnail,
							cacheKey = cacheKey,
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
