package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.ui.view.getAnnotatedUrl

@Composable
fun ExternalResourceDialog(
	url: String,
	onConfirmClick: () -> Unit,
	onCancelClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		titleText = stringResource(id = R.string.dialog_title_warning_external),
		positiveText = stringResource(id = R.string.accept),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = onConfirmClick,
		onNegativeClick = onCancelClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_16)),
			text = stringResource(id = R.string.dialog_message_warning_external),
			style = MaterialTheme.typography.bodyMedium
		)
		Text(
			text = getAnnotatedUrl(url = url),
			style = MaterialTheme.typography.bodyMedium
		)
	}
}