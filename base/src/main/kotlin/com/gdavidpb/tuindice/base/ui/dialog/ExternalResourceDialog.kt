package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.ui.view.getAnnotatedUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalResourceDialog(
	sheetState: SheetState,
	url: String,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.dialog_title_warning_external),
		positiveText = stringResource(id = R.string.accept),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = onConfirmClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		Text(
			text = stringResource(id = R.string.dialog_message_warning_external),
			style = MaterialTheme.typography.bodyMedium
		)
		Text(
			text = getAnnotatedUrl(url = url),
			style = MaterialTheme.typography.bodyMedium
		)
	}
}