package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutDialog(
	state: SignOut.State,
	titleText: String,
	messageText: String,
	confirmText: String,
	cancelText: String,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		dismissOnPositive = false,
		titleText = titleText,
		positiveText = confirmText,
		negativeText = cancelText,
		positiveLoading = state is SignOut.State.LoggingOut,
		positiveEnabled = state is SignOut.State.Idle,
		negativeEnabled = state is SignOut.State.Idle,
		onPositiveClick = onConfirmClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		Text(
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
