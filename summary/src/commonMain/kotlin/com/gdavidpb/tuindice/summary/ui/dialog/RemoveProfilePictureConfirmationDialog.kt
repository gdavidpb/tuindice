package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.dialog.rememberTuIndiceSheetState
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

@Composable
fun RemoveProfilePictureConfirmationDialog(
	titleText: String,
	messageText: String,
	confirmText: String,
	cancelText: String,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberTuIndiceSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = titleText,
		dismissOnPositive = false,
		positiveText = confirmText,
		negativeText = cancelText,
		onPositiveClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(SummaryUiTags.RemoveProfilePictureMessage),
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
