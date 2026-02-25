package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveProfilePictureConfirmationDialog(
	titleText: String,
	messageText: String,
	confirmText: String,
	cancelText: String,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = titleText,
		positiveText = confirmText,
		negativeText = cancelText,
		onPositiveClick = onConfirmClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		Text(
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
