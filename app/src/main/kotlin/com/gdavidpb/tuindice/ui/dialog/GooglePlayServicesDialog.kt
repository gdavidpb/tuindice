package com.gdavidpb.tuindice.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.login.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlayServicesDialog(
	sheetState: SheetState,
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.dialog_title_no_gms_failure),
		positiveText = stringResource(id = R.string.exit),
		onPositiveClick = onConfirmExitClick,
		onDismissRequest = onDismissRequest
	) {
		Text(text = stringResource(id = R.string.dialog_message_no_gms_failure))
	}
}