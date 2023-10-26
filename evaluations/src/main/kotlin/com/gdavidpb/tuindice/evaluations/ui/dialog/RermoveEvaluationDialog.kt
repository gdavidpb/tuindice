package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveEvaluationDialog(
	sheetState: SheetState,
	evaluation: EvaluationItem,
	onConfirmClick: (evaluation: EvaluationItem) -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.dialog_title_remove_evaluation),
		positiveText = stringResource(id = R.string.dialog_button_remove_evaluation),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = { onConfirmClick(evaluation) },
		onDismissRequest = onDismissRequest
	) {
		val message = stringResource(
			id = R.string.dialog_message_remove_evaluation,
			evaluation.name,
			evaluation.subjectCode
		)

		Text(
			text = buildAnnotatedString {
				append(message)

				val start = message.indexOf(evaluation.name)
				val end = message.indexOf(evaluation.subjectCode) + evaluation.subjectCode.length

				addStyle(
					style = SpanStyle(fontWeight = FontWeight.Bold),
					start = start,
					end = end
				)
			},
			style = MaterialTheme.typography.bodyLarge
		)
	}
}