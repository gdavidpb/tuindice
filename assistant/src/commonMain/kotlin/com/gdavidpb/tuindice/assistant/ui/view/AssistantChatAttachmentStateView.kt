package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.model.ChatAttachmentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_not_uploaded
import tuindice.assistant.generated.resources.assistant_attachment_retry
import tuindice.assistant.generated.resources.assistant_attachment_uploading

private val PROGRESS_SIZE = 28.dp
private val PROGRESS_STROKE = 2.dp
private const val VEIL_ALPHA = 0.45f

// The veil over an attachment of the tray, the size of the attachment (the caller fills it): a progress while it is
// read or sent, and the error's colour for one that failed. A failure that can clear on its own offers the retry; the
// veil names the attachment and says it was not sent; the reason is the line under the tray.
@Composable
fun AssistantChatAttachmentStateView(
	item: ChatAttachmentItem,
	onRetry: () -> Unit,
	modifier: Modifier = Modifier
) {
	val uploadingDescription = stringResource(Res.string.assistant_attachment_uploading, item.name)
	val failedDescription = stringResource(Res.string.assistant_attachment_not_uploaded, item.name)

	if (item.isUploading) {
		Box(
			modifier = modifier
				.semantics { contentDescription = uploadingDescription }
				.clip(RoundedCornerShape(TuIndiceRadius.Medium))
				.background(MaterialTheme.colorScheme.scrim.copy(alpha = VEIL_ALPHA)),
			contentAlignment = Alignment.Center
		) {
			CircularProgressIndicator(
				modifier = Modifier.size(PROGRESS_SIZE),
				color = MaterialTheme.colorScheme.onPrimary,
				strokeWidth = PROGRESS_STROKE
			)
		}
	}

	if (item.isFailed) {
		Box(
			modifier = modifier
				.semantics { contentDescription = failedDescription }
				.clip(RoundedCornerShape(TuIndiceRadius.Medium))
				.background(MaterialTheme.colorScheme.error.copy(alpha = VEIL_ALPHA)),
			contentAlignment = Alignment.Center
		) {
			if (item.canRetry) {
				IconButton(
					onClick = onRetry,
					modifier = Modifier.testTag(AssistantUiTags.AttachmentRetry)
				) {
					Icon(
						imageVector = Icons.Outlined.Refresh,
						contentDescription = stringResource(Res.string.assistant_attachment_retry, item.name),
						tint = MaterialTheme.colorScheme.onError
					)
				}
			}
		}
	}
}
