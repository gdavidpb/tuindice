package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.evaluations.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveEvaluationDialog(
	sheetState: SheetState,
	evaluationId: String,
	onConfirmClick: (evaluationId: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.dialog_title_remove_evaluation),
		positiveText = stringResource(id = R.string.accept),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = { onConfirmClick(evaluationId) },
		onDismissRequest = onDismissRequest
	) {
		Text(
			text = stringResource(id = R.string.dialog_message_remove_evaluation),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}