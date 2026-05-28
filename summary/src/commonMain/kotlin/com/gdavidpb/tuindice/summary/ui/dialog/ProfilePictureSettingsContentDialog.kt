package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.menu_pick_profile_picture
import tuindice.summary.generated.resources.menu_remove_profile_picture
import tuindice.summary.generated.resources.menu_take_profile_picture
import tuindice.summary.generated.resources.title_menu_profile_picture

@Composable
fun ProfilePictureSettingsContentDialog(
	showRemove: Boolean,
	isCameraAvailable: Boolean,
	onPickPictureClick: () -> Unit,
	onTakePictureClick: () -> Unit,
	onRemovePictureClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ProfilePictureSettingsDialog(
		showRemove = showRemove,
		isCameraAvailable = isCameraAvailable,
		titleText = stringResource(Res.string.title_menu_profile_picture),
		pickPictureLabel = stringResource(Res.string.menu_pick_profile_picture),
		takePictureLabel = stringResource(Res.string.menu_take_profile_picture),
		removePictureLabel = stringResource(Res.string.menu_remove_profile_picture),
		onPickPictureClick = onPickPictureClick,
		onTakePictureClick = onTakePictureClick,
		onRemovePictureClick = onRemovePictureClick,
		onDismissRequest = onDismissRequest
	)
}
