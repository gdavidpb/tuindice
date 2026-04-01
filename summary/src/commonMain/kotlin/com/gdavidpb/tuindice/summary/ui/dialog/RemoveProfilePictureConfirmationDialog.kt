package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

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
