package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_photo_add
import tuindice.assistant.generated.resources.assistant_photo_pick
import tuindice.assistant.generated.resources.assistant_photo_take

// The photo button: a clip inside the composer's field, at its end. It opens a small menu: the camera, when the device
// has one, and the gallery. It is off while a turn or a photo is being read.
@Composable
fun AssistantChatPhotoButtonView(
	isEnabled: Boolean,
	isCameraAvailable: Boolean,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val menuOpen = remember { mutableStateOf(false) }

	Box(modifier = modifier) {
		IconButton(
			onClick = { menuOpen.value = true },
			enabled = isEnabled,
			modifier = Modifier.testTag(AssistantUiTags.PhotoButton)
		) {
			Icon(
				imageVector = Icons.Outlined.AttachFile,
				contentDescription = stringResource(Res.string.assistant_photo_add)
			)
		}

		DropdownMenu(
			expanded = menuOpen.value,
			onDismissRequest = { menuOpen.value = false }
		) {
			if (isCameraAvailable) {
				DropdownMenuItem(
					text = { Text(text = stringResource(Res.string.assistant_photo_take)) },
					onClick = {
						menuOpen.value = false
						onAction(AssistantChat.Action.RequestPhoto(fromCamera = true))
					},
					modifier = Modifier.testTag(AssistantUiTags.PhotoTake)
				)
			}

			DropdownMenuItem(
				text = { Text(text = stringResource(Res.string.assistant_photo_pick)) },
				onClick = {
					menuOpen.value = false
					onAction(AssistantChat.Action.RequestPhoto(fromCamera = false))
				},
				modifier = Modifier.testTag(AssistantUiTags.PhotoPick)
			)
		}
	}
}
