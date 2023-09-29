package com.gdavidpb.tuindice.login.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.login.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		dismissOnPositive = false,
		titleText = stringResource(id = R.string.dialog_title_sign_out),
		positiveText = stringResource(id = R.string.dialog_button_sign_out),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = onConfirmClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		Text(
			text = stringResource(id = R.string.dialog_message_sign_out),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}