package com.gdavidpb.tuindice.login.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.ui.view.UpdatePasswordIdleView
import com.gdavidpb.tuindice.login.ui.view.UpdatePasswordLoggingInView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun UpdatePasswordDialog(
	state: UpdatePassword.State,
	onPasswordChanged: (password: String) -> Unit,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val isConfirmEnabled = state is UpdatePassword.State.Idle && state.password.isNotEmpty()
	val isLaterEnabled = state is UpdatePassword.State.Idle
	val isLoading = state is UpdatePassword.State.Updating

	val nonDismissSheetState = rememberModalBottomSheetState(
		confirmValueChange = { false }
	)

	ConfirmationDialog(
		sheetState = nonDismissSheetState,
		titleText = if (isLoading)
			stringResource(id = R.string.dialog_title_updating_password)
		else
			stringResource(id = R.string.dialog_title_update_password),
		dismissOnPositive = false,
		positiveEnabled = isConfirmEnabled,
		negativeEnabled = isLaterEnabled,
		positiveText = stringResource(id = R.string.dialog_button_update_password_confirm),
		negativeText = stringResource(id = R.string.dialog_button_update_password_later),
		onPositiveClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	) {
		AnimatedContent(
			targetState = isLoading,
			transitionSpec = {
				val enter = slideInHorizontally { x -> x }
				val exit = slideOutHorizontally { x -> -x }

				enter with exit
			},
		) { isLoggingIn ->
			if (isLoggingIn)
				UpdatePasswordLoggingInView()
			else
				UpdatePasswordIdleView(
					state = state,
					onPasswordChanged = onPasswordChanged,
					onConfirmClick = onConfirmClick
				)
		}
	}
}