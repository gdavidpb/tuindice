package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.cancel
import tuindice.evaluations.generated.resources.dialog_button_delete
import tuindice.evaluations.generated.resources.dialog_message_delete_evaluation
import tuindice.evaluations.generated.resources.dialog_title_delete_evaluation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteEvaluationConfirmationContentDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.dialog_title_delete_evaluation),
		dismissOnPositive = false,
		positiveText = stringResource(Res.string.dialog_button_delete),
		negativeText = stringResource(Res.string.cancel),
		onPositiveClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(EvaluationsUiTags.DeleteEvaluationMessage),
			text = stringResource(Res.string.dialog_message_delete_evaluation),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
