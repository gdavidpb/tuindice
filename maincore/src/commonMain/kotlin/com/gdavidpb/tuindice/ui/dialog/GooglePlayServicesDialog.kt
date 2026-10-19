package com.gdavidpb.tuindice.ui.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.base.ui.dialog.rememberTuIndiceSheetState
import com.gdavidpb.tuindice.ui.MaincoreUiTags

@Composable
fun GooglePlayServicesDialog(
	titleText: String,
	messageText: String,
	exitText: String,
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val nonDismissSheetState = rememberTuIndiceSheetState(hideable = false)

	ConfirmationDialog(
		sheetState = nonDismissSheetState,
		titleText = titleText,
		positiveText = exitText,
		onPositiveClick = onConfirmExitClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(MaincoreUiTags.GooglePlayServicesMessage),
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
