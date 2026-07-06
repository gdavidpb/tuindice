package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.dialog_button_cancel
import tuindice.record.generated.resources.dialog_button_discard
import tuindice.record.generated.resources.dialog_message_discard_synthetic_term
import tuindice.record.generated.resources.dialog_title_discard_synthetic_term

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscardSyntheticTermContentDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.dialog_title_discard_synthetic_term),
		dismissOnPositive = false,
		positiveText = stringResource(Res.string.dialog_button_discard),
		negativeText = stringResource(Res.string.dialog_button_cancel),
		onPositiveClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(RecordUiTags.DiscardSyntheticTermMessage),
			text = stringResource(Res.string.dialog_message_discard_synthetic_term),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
