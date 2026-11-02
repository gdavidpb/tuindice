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
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_retry
import tuindice.assistant.generated.resources.assistant_attachment_uploading

private val TILE_SIZE = 56.dp
private val PROGRESS_SIZE = 28.dp
private val PROGRESS_STROKE = 2.dp
private const val VEIL_ALPHA = 0.45f

// The veil over an attachment of the tray while it is read or sent: a progress for a wait, and the error's colour
// with the retry for an attachment that failed.
@Composable
fun AssistantChatAttachmentStateView(
	name: String,
	isUploading: Boolean,
	isFailed: Boolean,
	onRetry: () -> Unit,
	modifier: Modifier = Modifier
) {
	val uploadingDescription = stringResource(Res.string.assistant_attachment_uploading, name)

	if (isUploading) {
		Box(
			modifier = modifier
				.size(TILE_SIZE)
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

	if (isFailed) {
		Box(
			modifier = modifier
				.size(TILE_SIZE)
				.clip(RoundedCornerShape(TuIndiceRadius.Medium))
				.background(MaterialTheme.colorScheme.error.copy(alpha = VEIL_ALPHA)),
			contentAlignment = Alignment.Center
		) {
			IconButton(
				onClick = onRetry,
				modifier = Modifier.testTag(AssistantUiTags.AttachmentRetry)
			) {
				Icon(
					imageVector = Icons.Outlined.Refresh,
					contentDescription = stringResource(Res.string.assistant_attachment_retry, name),
					tint = MaterialTheme.colorScheme.onError
				)
			}
		}
	}
}
