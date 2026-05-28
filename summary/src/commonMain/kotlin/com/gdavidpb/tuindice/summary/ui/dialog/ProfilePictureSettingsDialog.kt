package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialogEntry
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureSettingsDialog(
	showRemove: Boolean,
	isCameraAvailable: Boolean,
	titleText: String,
	pickPictureLabel: String,
	takePictureLabel: String,
	removePictureLabel: String,
	onPickPictureClick: () -> Unit,
	onTakePictureClick: () -> Unit,
	onRemovePictureClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = titleText,
		onDismissRequest = onDismissRequest
	) {
		Box(modifier = Modifier.testTag(SummaryUiTags.ProfilePicturePickAction)) {
			ConfirmationDialogEntry(
				icon = Icons.Outlined.UploadFile,
				text = pickPictureLabel,
				onClick = onPickPictureClick
			)
		}

		if (isCameraAvailable)
			Box(modifier = Modifier.testTag(SummaryUiTags.ProfilePictureTakeAction)) {
				ConfirmationDialogEntry(
					icon = Icons.Outlined.PhotoCamera,
					text = takePictureLabel,
					onClick = onTakePictureClick
				)
			}

		if (showRemove)
			Box(modifier = Modifier.testTag(SummaryUiTags.ProfilePictureRemoveAction)) {
				ConfirmationDialogEntry(
					icon = Icons.Outlined.Delete,
					iconColor = MaterialTheme.colorScheme.error,
					text = removePictureLabel,
					textColor = MaterialTheme.colorScheme.error,
					onClick = onRemovePictureClick
				)
			}

		Spacer(
			modifier = Modifier
				.size(16.dp)
		)
	}
}
