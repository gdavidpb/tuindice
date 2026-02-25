package com.gdavidpb.tuindice.login.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePasswordDialog(
	state: UpdatePassword.State,
	titleText: String,
	updatingTitleText: String,
	confirmText: String,
	laterText: String,
	onPasswordChange: (password: String) -> Unit,
	onConfirmClick: (password: String) -> Unit,
	onDismissRequest: () -> Unit,
	idleContent: @Composable (
		state: UpdatePassword.State,
		onPasswordChange: (password: String) -> Unit,
		onConfirmClick: (password: String) -> Unit
	) -> Unit,
	updatingContent: @Composable () -> Unit
) {
	val isConfirmEnabled = state is UpdatePassword.State.Idle && state.password.isNotEmpty()
	val isLaterEnabled = state is UpdatePassword.State.Idle
	val isLoading = state is UpdatePassword.State.Updating

	val nonDismissSheetState = rememberModalBottomSheetState(
		confirmValueChange = { sheetValue ->
			sheetValue != SheetValue.Hidden
		}
	)

	ConfirmationDialog(
		sheetState = nonDismissSheetState,
		dismissOnPositive = false,
		titleText = if (isLoading) updatingTitleText else titleText,
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
		AnimatedContent(
			targetState = isLoading,
			transitionSpec = {
				val enter = slideInHorizontally { x -> x }
				val exit = slideOutHorizontally { x -> -x }

				enter togetherWith exit
			},
			label = "ConfirmationDialogAnimatedContent",
		) { isLoggingIn ->
			if (isLoggingIn)
				updatingContent()
			else
				idleContent(
					state,
					onPasswordChange,
					onConfirmClick
				)
		}
	}
}
