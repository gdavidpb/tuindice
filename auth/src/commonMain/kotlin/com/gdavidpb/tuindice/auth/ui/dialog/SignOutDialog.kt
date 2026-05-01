package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutDialog(
	state: SignOut.State,
	titleText: String,
	messageText: String,
	confirmText: String,
	secondaryText: String?,
	cancelText: String,
	onConfirmClick: () -> Unit,
	onSecondaryClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()
	val isLoggingOut = state is SignOut.State.LoggingOut

	ModalBottomSheet(
		modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogSheet),
		sheetState = sheetState,
		onDismissRequest = {
			if (!isLoggingOut) {
				onDismissRequest()
			}
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
		) {
			Text(
				modifier = Modifier
					.testTag(BaseUiTags.ConfirmationDialogTitle)
					.padding(bottom = 16.dp),
				text = titleText,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)

			Text(
				modifier = Modifier.testTag(AuthUiTags.SignOutMessageText),
				text = messageText,
				style = MaterialTheme.typography.bodyLarge
			)

			SignOutDialogActions(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 24.dp),
				confirmText = confirmText,
				secondaryText = secondaryText,
				cancelText = cancelText,
				isLoggingOut = isLoggingOut,
				onConfirmClick = onConfirmClick,
				onSecondaryClick = onSecondaryClick,
				onDismissRequest = onDismissRequest
			)
		}
	}
}

@Composable
private fun SignOutDialogActions(
	modifier: Modifier = Modifier,
	confirmText: String,
	secondaryText: String?,
	cancelText: String,
	isLoggingOut: Boolean,
	onConfirmClick: () -> Unit,
	onSecondaryClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	if (secondaryText == null) {
		Row(
			modifier = modifier,
			horizontalArrangement = Arrangement.End
		) {
			CancelButton(
				text = cancelText,
				enabled = !isLoggingOut,
				onClick = onDismissRequest
			)

			ConfirmButton(
				text = confirmText,
				isLoggingOut = isLoggingOut,
				onClick = onConfirmClick
			)
		}
	} else {
		Column(
			modifier = modifier,
			horizontalAlignment = Alignment.End
		) {
			ConfirmButton(
				modifier = Modifier.fillMaxWidth(),
				text = confirmText,
				isLoggingOut = isLoggingOut,
				onClick = onConfirmClick
			)

			Row(
				modifier = Modifier.padding(top = 8.dp),
				horizontalArrangement = Arrangement.End
			) {
				CancelButton(
					text = cancelText,
					enabled = !isLoggingOut,
					onClick = onDismissRequest
				)

				SecondaryButton(
					text = secondaryText,
					enabled = !isLoggingOut,
					onClick = onSecondaryClick
				)
			}
		}
	}
}

@Composable
private fun CancelButton(
	text: String,
	enabled: Boolean,
	onClick: () -> Unit
) {
	OutlinedButton(
		modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogNegativeButton),
		onClick = onClick,
		border = null,
		enabled = enabled
	) {
		Text(text = text)
	}
}

@Composable
private fun SecondaryButton(
	text: String,
	enabled: Boolean,
	onClick: () -> Unit
) {
	OutlinedButton(
		modifier = Modifier.testTag(AuthUiTags.SignOutSecondaryButton),
		onClick = onClick,
		border = null,
		enabled = enabled
	) {
		Text(text = text)
	}
}

@Composable
private fun ConfirmButton(
	modifier: Modifier = Modifier,
	text: String,
	isLoggingOut: Boolean,
	onClick: () -> Unit
) {
	Button(
		modifier = modifier.testTag(BaseUiTags.ConfirmationDialogPositiveButton),
		onClick = onClick,
		enabled = !isLoggingOut
	) {
		Box(
			contentAlignment = Alignment.Center
		) {
			Text(
				text = text,
				color = if (isLoggingOut) {
					Color.Transparent
				} else {
					Color.Unspecified
				}
			)

			if (isLoggingOut) {
				CircularProgressIndicator(
					modifier = Modifier
						.testTag(BaseUiTags.ConfirmationDialogPositiveLoading)
						.size(18.dp),
					color = Color.White
				)
			}
		}
	}
}
