package com.gdavidpb.tuindice.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.login.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlayServicesDialog(
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
		titleText = stringResource(id = R.string.dialog_title_no_gms_failure),
		positiveText = stringResource(id = R.string.exit),
		onPositiveClick = onConfirmExitClick,
		onDismissRequest = onDismissRequest,
		properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
	) {
		Text(
			text = stringResource(id = R.string.dialog_message_no_gms_failure),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}