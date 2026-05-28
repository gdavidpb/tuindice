package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.ui.view.UpdatePasswordIdleView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePasswordDialog(
	state: UpdatePassword.State,
	titleText: String,
	confirmText: String,
	laterText: String,
	appNameText: String,
	messageText: String,
	passwordLabelText: String,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
	onConfirmClick: (password: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val isLoading = state is UpdatePassword.State.Updating
	val contentState = when (state) {
		is UpdatePassword.State.Idle -> state
		is UpdatePassword.State.Updating ->
			UpdatePassword.State.Idle(
				password = state.password,
				isPasswordVisible = state.isPasswordVisible
			)
	}
	val isConfirmEnabled = !isLoading && contentState.password.isNotEmpty()
	val isLaterEnabled = !isLoading

	val nonDismissSheetState = rememberModalBottomSheetState(
		confirmValueChange = { sheetValue ->
			sheetValue != SheetValue.Hidden
		}
	)

	ConfirmationDialog(
		sheetState = nonDismissSheetState,
		dismissOnPositive = false,
		titleText = titleText,
		positiveLoading = isLoading,
		positiveEnabled = isConfirmEnabled,
		negativeEnabled = isLaterEnabled,
		positiveText = confirmText,
		negativeText = laterText,
		onPositiveClick = {
			if (state is UpdatePassword.State.Idle)
				onConfirmClick(state.password)
		},
		onDismissRequest = onDismissRequest,
		properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
	) {
		UpdatePasswordIdleView(
			state = contentState,
			enabled = !isLoading,
			onPasswordChange = onPasswordChange,
			onPasswordVisibilityToggle = onPasswordVisibilityToggle,
			onConfirmClick = onConfirmClick,
			appNameText = appNameText,
			messageText = messageText,
			passwordLabelText = passwordLabelText
		)
	}
}
