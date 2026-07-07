package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.dialog_button_cancel
import tuindice.auth.generated.resources.dialog_button_retry
import tuindice.auth.generated.resources.dialog_button_sign_out
import tuindice.auth.generated.resources.dialog_button_sign_out_and_sync
import tuindice.auth.generated.resources.dialog_button_sign_out_anyway
import tuindice.auth.generated.resources.dialog_button_update_password
import tuindice.auth.generated.resources.dialog_message_sign_out
import tuindice.auth.generated.resources.dialog_message_sign_out_flush_failed
import tuindice.auth.generated.resources.dialog_message_sign_out_outdated_credentials
import tuindice.auth.generated.resources.dialog_message_sign_out_pending
import tuindice.auth.generated.resources.dialog_title_sign_out

@Composable
fun SignOutContentDialog(
	state: SignOut.State,
	onConfirmClick: () -> Unit,
	onSecondaryClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val pendingChanges = when (state) {
		is SignOut.State.Pending -> state.pendingChanges
		is SignOut.State.FlushFailed -> state.pendingChanges
		is SignOut.State.LoggingOut -> state.pendingChanges
		SignOut.State.Plain -> null
	}
	val messageText = when (state) {
		SignOut.State.Plain ->
			stringResource(Res.string.dialog_message_sign_out)

		is SignOut.State.Pending ->
			pluralStringResource(
				Res.plurals.dialog_message_sign_out_pending,
				state.pendingChanges.totalCount,
				state.pendingChanges.totalCount
			)

		is SignOut.State.FlushFailed ->
			if (state.requiresPasswordUpdate) {
				pluralStringResource(
					Res.plurals.dialog_message_sign_out_outdated_credentials,
					state.pendingChanges.totalCount,
					state.pendingChanges.totalCount
				)
			} else {
				pluralStringResource(
					Res.plurals.dialog_message_sign_out_flush_failed,
					state.pendingChanges.totalCount,
					state.pendingChanges.totalCount
				)
			}

		is SignOut.State.LoggingOut ->
			when {
				state.requiresPasswordUpdate && pendingChanges != null ->
					pluralStringResource(
						Res.plurals.dialog_message_sign_out_outdated_credentials,
						pendingChanges.totalCount,
						pendingChanges.totalCount
					)

				pendingChanges != null ->
					pluralStringResource(
						Res.plurals.dialog_message_sign_out_pending,
						pendingChanges.totalCount,
						pendingChanges.totalCount
					)

				else ->
					stringResource(Res.string.dialog_message_sign_out)
			}
	}
	val primaryText = when (state) {
		SignOut.State.Plain,
		is SignOut.State.LoggingOut,
		-> stringResource(Res.string.dialog_button_sign_out)

		is SignOut.State.Pending ->
			stringResource(Res.string.dialog_button_sign_out_and_sync)

		is SignOut.State.FlushFailed ->
			if (state.requiresPasswordUpdate) {
				stringResource(Res.string.dialog_button_update_password)
			} else {
				stringResource(Res.string.dialog_button_retry)
			}
	}
	val secondaryText = when (state) {
		is SignOut.State.FlushFailed ->
			stringResource(Res.string.dialog_button_sign_out_anyway)

		SignOut.State.Plain,
		is SignOut.State.Pending,
		is SignOut.State.LoggingOut,
		-> null
	}

	SignOutDialog(
		state = state,
		titleText = stringResource(Res.string.dialog_title_sign_out),
		messageText = messageText,
		confirmText = primaryText,
		secondaryText = secondaryText,
		cancelText = stringResource(Res.string.dialog_button_cancel),
		onConfirmClick = onConfirmClick,
		onSecondaryClick = onSecondaryClick,
		onDismissRequest = onDismissRequest
	)
}
