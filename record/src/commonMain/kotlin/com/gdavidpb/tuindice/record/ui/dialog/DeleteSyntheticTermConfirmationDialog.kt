package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSyntheticTermConfirmationDialog(
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
			modifier = Modifier.testTag(RecordUiTags.DeleteSyntheticTermMessage),
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
