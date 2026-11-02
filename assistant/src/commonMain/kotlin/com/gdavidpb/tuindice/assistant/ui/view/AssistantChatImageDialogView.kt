package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_attachment_close
import tuindice.assistant.generated.resources.assistant_attachment_photo
import org.jetbrains.compose.resources.stringResource as composeStringResource

// A sent photo at full size, over the whole screen. It closes with its × or with the system's back.
@Composable
fun AssistantChatImageDialogView(
	image: ByteArray,
	onDismiss: () -> Unit
) {
	val photoDescription = composeStringResource(Res.string.assistant_attachment_photo)
	val closeDescription = composeStringResource(Res.string.assistant_attachment_close)

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false)
	) {
		Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
			AssistantChatThumbnailView(
				bytes = image,
				key = image.contentHashCode().toString(),
				contentDescription = photoDescription,
				contentScale = ContentScale.Fit
			)

			IconButton(
				onClick = onDismiss,
				modifier = Modifier.align(Alignment.TopEnd).padding(TuIndiceSpacing.Small)
			) {
				Icon(
					imageVector = Icons.Outlined.Close,
					contentDescription = closeDescription,
					tint = Color.White
				)
			}
		}
	}
}
