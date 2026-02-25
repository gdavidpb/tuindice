package com.gdavidpb.tuindice.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlayServicesDialog(
	titleText: String,
	messageText: String,
	exitText: String,
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val nonDismissSheetState = rememberModalBottomSheetState(
		confirmValueChange = { sheetValue ->
			sheetValue != SheetValue.Hidden
		}
	)

	ConfirmationDialog(
		sheetState = nonDismissSheetState,
		titleText = titleText,
		positiveText = exitText,
		onPositiveClick = onConfirmExitClick,
		onDismissRequest = onDismissRequest,
		properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
	) {
		Text(
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
