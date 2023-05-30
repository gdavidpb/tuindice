package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialogEntry
import com.gdavidpb.tuindice.summary.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureSettingsDialog(
	sheetState: SheetState,
	showTake: Boolean,
	showRemove: Boolean,
	onPickPictureClick: () -> Unit,
	onTakePictureClick: () -> Unit,
	onRemovePictureClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val coroutineScope = rememberCoroutineScope()

	val dismiss = fun(onComplete: () -> Unit) {
		coroutineScope.launch {
			sheetState.hide()
		}.invokeOnCompletion {
			onDismissRequest()
			onComplete()
		}
	}

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.title_menu_profile_picture),
		onDismissRequest = onDismissRequest
	) {
		ConfirmationDialogEntry(
			icon = Icons.Outlined.UploadFile,
			text = stringResource(id = R.string.menu_pick_profile_picture),
			onClick = { dismiss(onPickPictureClick) }
		)

		if (showTake)
			ConfirmationDialogEntry(
				icon = Icons.Outlined.PhotoCamera,
				text = stringResource(id = R.string.menu_take_profile_picture),
				onClick = { dismiss(onTakePictureClick) }
			)

		if (showRemove)
			ConfirmationDialogEntry(
				icon = Icons.Outlined.Delete,
				iconColor = MaterialTheme.colorScheme.error,
				text = stringResource(id = R.string.menu_remove_profile_picture),
				textColor = MaterialTheme.colorScheme.error,
				onClick = { dismiss(onRemovePictureClick) }
			)

		Spacer(
			modifier = Modifier
				.size(dimensionResource(id = R.dimen.dp_16))
		)
	}
}