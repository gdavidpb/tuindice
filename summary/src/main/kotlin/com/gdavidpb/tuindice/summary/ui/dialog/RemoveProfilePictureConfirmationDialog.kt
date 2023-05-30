package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.summary.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveProfilePictureConfirmationDialog(
	sheetState: SheetState,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.dialog_title_remove_profile_picture),
		positiveText = stringResource(id = R.string.remove),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = onConfirmClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		Text(text = stringResource(id = R.string.dialog_message_remove_profile_picture))
	}
}