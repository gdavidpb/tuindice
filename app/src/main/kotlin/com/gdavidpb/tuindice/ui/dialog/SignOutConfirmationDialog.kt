package com.gdavidpb.tuindice.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.summary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutConfirmationDialog(
	sheetState: SheetState,
	onConfirmSignOutClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.dialog_title_sign_out),
		positiveText = stringResource(id = R.string.dialog_button_sign_out),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = onConfirmSignOutClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		Text(text = stringResource(id = R.string.dialog_message_sign_out))
	}
}