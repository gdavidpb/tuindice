package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusInfoDialog(
	titleText: String,
	messageText: String,
	confirmText: String,
	dismissText: String? = null,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = titleText,
		positiveText = confirmText,
		negativeText = dismissText,
		onPositiveClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(SummaryUiTags.SyncStatusMessage),
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
